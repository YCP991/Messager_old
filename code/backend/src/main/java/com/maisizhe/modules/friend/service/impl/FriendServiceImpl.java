package com.maisizhe.modules.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.maisizhe.common.constants.RedisKeyConstants;
import com.maisizhe.modules.friend.dto.AddFriendDTO;
import com.maisizhe.modules.friend.dto.UpdateFriendRemarkDTO;
import com.maisizhe.modules.friend.dto.UpdateFriendSortDTO;
import com.maisizhe.modules.friend.entity.Friend;
import com.maisizhe.modules.friend.mapper.FriendMapper;
import com.maisizhe.modules.friend.service.FriendService;
import com.maisizhe.modules.friend.vo.FriendVO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 好友服务实现类
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    
    private final FriendMapper friendMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public List<FriendVO> getFriends(Long userId) {
        // 查询该用户的所有好友关系
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("is_pinned", "sort_order", "create_time");
        
        List<Friend> friends = friendMapper.selectList(queryWrapper);
        
        if (friends.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取好友ID列表
        List<Long> friendIds = friends.stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());
        
        // 批量查询好友信息
        List<User> users = userMapper.selectBatchIds(friendIds);
        
        // 构建好友ID到用户信息的映射
        java.util.Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        // 构建返回结果
        List<FriendVO> result = new ArrayList<>();
        for (Friend friend : friends) {
            User user = userMap.get(friend.getFriendId());
            if (user != null) {
                FriendVO vo = FriendVO.builder()
                        .friendId(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .avatar(user.getAvatar())
                        .classNo(user.getClassNo())
                        .department(user.getDepartment())
                        .remark(friend.getRemark())
                        .isPinned(friend.getIsPinned())
                        .isMuted(friend.getIsMuted())
                        .sortOrder(friend.getSortOrder())
                        .isOnline(checkUserOnline(user.getId()))
                        .createTime(friend.getCreateTime() != null ? 
                                friend.getCreateTime().toString() : null)
                        .build();
                result.add(vo);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean addFriend(Long userId, AddFriendDTO dto) {
        Long targetUserId = dto.getTargetUserId();
        
        // 不能添加自己为好友
        if (userId.equals(targetUserId)) {
            log.warn("用户 {} 试图添加自己为好友", userId);
            return false;
        }
        
        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            log.warn("目标用户 {} 不存在", targetUserId);
            return false;
        }
        
        // 检查是否已经是好友
        if (isFriend(userId, targetUserId)) {
            log.warn("用户 {} 和用户 {} 已经是好友", userId, targetUserId);
            return false;
        }
        
        // 创建好友关系(双向好友)
        LocalDateTime now = LocalDateTime.now();
        
        // A -> B
        Friend friend1 = new Friend();
        friend1.setUserId(userId);
        friend1.setFriendId(targetUserId);
        friend1.setRemark(dto.getRemark());
        friend1.setIsPinned(0);
        friend1.setIsMuted(0);
        friend1.setSortOrder((double) (System.currentTimeMillis() / 1000));
        friend1.setCreateTime(now);
        friend1.setUpdateTime(now);
        friendMapper.insert(friend1);
        
        // B -> A (互为好友)
        Friend friend2 = new Friend();
        friend2.setUserId(targetUserId);
        friend2.setFriendId(userId);
        friend2.setRemark("");
        friend2.setIsPinned(0);
        friend2.setIsMuted(0);
        friend2.setSortOrder((double) (System.currentTimeMillis() / 1000));
        friend2.setCreateTime(now);
        friend2.setUpdateTime(now);
        friendMapper.insert(friend2);
        
        log.info("用户 {} 添加好友 {} 成功", userId, targetUserId);
        return true;
    }
    
    @Override
    @Transactional
    public boolean deleteFriend(Long userId, Long friendId) {
        // 删除双向好友关系
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("friend_id", friendId);
        friendMapper.delete(queryWrapper);
        
        QueryWrapper<Friend> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("user_id", friendId).eq("friend_id", userId);
        friendMapper.delete(queryWrapper2);
        
        log.info("用户 {} 删除好友 {} 成功", userId, friendId);
        return true;
    }
    
    @Override
    public boolean updateRemark(Long userId, UpdateFriendRemarkDTO dto) {
        Friend friend = friendMapper.selectByUserAndFriend(userId, dto.getFriendId());
        if (friend == null) {
            log.warn("好友关系不存在: userId={}, friendId={}", userId, dto.getFriendId());
            return false;
        }
        
        friend.setRemark(dto.getRemark());
        friend.setUpdateTime(LocalDateTime.now());
        friendMapper.updateById(friend);
        
        log.info("用户 {} 更新好友 {} 备注成功", userId, dto.getFriendId());
        return true;
    }
    
    @Override
    public boolean updateSort(Long userId, UpdateFriendSortDTO dto) {
        Friend friend = friendMapper.selectByUserAndFriend(userId, dto.getFriendId());
        if (friend == null) {
            log.warn("好友关系不存在: userId={}, friendId={}", userId, dto.getFriendId());
            return false;
        }
        
        friend.setSortOrder(dto.getSortOrder());
        friend.setIsPinned(dto.getIsPinned());
        friend.setUpdateTime(LocalDateTime.now());
        friendMapper.updateById(friend);
        
        log.info("用户 {} 更新好友 {} 排序成功", userId, dto.getFriendId());
        return true;
    }
    
    @Override
    @Transactional
    public boolean batchUpdateSort(Long userId, List<UpdateFriendSortDTO> sortList) {
        for (UpdateFriendSortDTO dto : sortList) {
            updateSort(userId, dto);
        }
        return true;
    }
    
    @Override
    public List<FriendVO> searchUsers(String keyword, Long excludeUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 模糊搜索用户名、学号、真实姓名
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(w -> w
                .like("username", keyword)
                .or().like("student_no", keyword)
                .or().like("real_name", keyword)
        );
        queryWrapper.ne("id", excludeUserId); // 排除自己
        queryWrapper.eq("status", 1); // 只查询正常状态的用户
        
        List<User> users = userMapper.selectList(queryWrapper);
        
        return users.stream().map(user -> FriendVO.builder()
                .friendId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .classNo(user.getClassNo())
                .department(user.getDepartment())
                .isOnline(checkUserOnline(user.getId()))
                .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public FriendVO getFriendDetail(Long userId, Long friendId) {
        Friend friend = friendMapper.selectByUserAndFriend(userId, friendId);
        if (friend == null) {
            return null;
        }
        
        User user = userMapper.selectById(friendId);
        if (user == null) {
            return null;
        }
        
        return FriendVO.builder()
                .friendId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .classNo(user.getClassNo())
                .department(user.getDepartment())
                .remark(friend.getRemark())
                .isPinned(friend.getIsPinned())
                .isMuted(friend.getIsMuted())
                .sortOrder(friend.getSortOrder())
                .isOnline(checkUserOnline(user.getId()))
                .createTime(friend.getCreateTime() != null ? 
                        friend.getCreateTime().toString() : null)
                .build();
    }
    
    @Override
    public boolean isFriend(Long userId, Long friendId) {
        return friendMapper.selectFriendCount(userId, friendId) > 0;
    }
    
    /**
     * 检查用户是否在线
     */
    private Integer checkUserOnline(Long userId) {
        String onlineKey = RedisKeyConstants.ONLINE_USER + userId;
        Boolean hasKey = redisTemplate.hasKey(onlineKey);
        return (hasKey != null && hasKey) ? 1 : 0;
    }
}
