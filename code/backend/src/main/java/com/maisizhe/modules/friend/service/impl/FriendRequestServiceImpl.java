package com.maisizhe.modules.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.maisizhe.modules.friend.entity.Friend;
import com.maisizhe.modules.friend.entity.FriendRequest;
import com.maisizhe.modules.friend.mapper.FriendMapper;
import com.maisizhe.modules.friend.mapper.FriendRequestMapper;
import com.maisizhe.modules.friend.service.FriendRequestService;
import com.maisizhe.modules.friend.vo.FriendRequestVO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 好友请求服务实现类
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {
    
    private final FriendRequestMapper friendRequestMapper;
    private final FriendMapper friendMapper;
    private final UserMapper userMapper;
    
    // 请求状态常量
    private static final Integer STATUS_PENDING = 0;
    private static final Integer STATUS_ACCEPTED = 1;
    private static final Integer STATUS_REJECTED = 2;
    private static final Integer STATUS_EXPIRED = 3;
    
    @Override
    @Transactional
    public boolean sendRequest(Long fromUserId, Long toUserId, String remark) {
        // 检查是否已存在待处理的请求
        int existingCount = friendRequestMapper.countByFromAndTo(fromUserId, toUserId, STATUS_PENDING);
        if (existingCount > 0) {
            log.warn("好友请求已存在: fromUserId={}, toUserId={}", fromUserId, toUserId);
            return false;
        }
        
        // 检查是否已是好友
        QueryWrapper<Friend> friendQuery = new QueryWrapper<>();
        friendQuery.eq("user_id", fromUserId).eq("friend_id", toUserId);
        if (friendMapper.selectCount(friendQuery) > 0) {
            log.warn("已是好友关系: userId={}, friendId={}", fromUserId, toUserId);
            return false;
        }
        
        // 创建好友请求（默认7天后过期）
        FriendRequest request = FriendRequest.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .remark(remark)
                .status(STATUS_PENDING)
                .expiresTime(LocalDateTime.now().plusDays(7))
                .build();
        
        int result = friendRequestMapper.insert(request);
        log.info("发送好友请求成功: requestId={}, fromUserId={}, toUserId={}", 
                request.getId(), fromUserId, toUserId);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean handleRequest(Long requestId, Long userId, boolean accept) {
        // 查询请求
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            log.warn("好友请求不存在: requestId={}", requestId);
            return false;
        }
        
        // 验证接收者身份
        if (!request.getToUserId().equals(userId)) {
            log.warn("无权处理此请求: userId={}, requestToUserId={}", userId, request.getToUserId());
            return false;
        }
        
        // 更新请求状态
        if (accept) {
            request.setStatus(STATUS_ACCEPTED);
            
            // 创建双向好友关系
            Friend friend1 = Friend.builder()
                    .userId(request.getFromUserId())
                    .friendId(request.getToUserId())
                    .isPinned(0)
                    .sortOrder(0.0)
                    .remark(request.getRemark())
                    .build();
            
            Friend friend2 = Friend.builder()
                    .userId(request.getToUserId())
                    .friendId(request.getFromUserId())
                    .isPinned(0)
                    .sortOrder(0.0)
                    .remark(null)
                    .build();
            
            friendMapper.insert(friend1);
            friendMapper.insert(friend2);
            
            log.info("好友请求已同意: requestId={}, fromUserId={}, toUserId={}", 
                    requestId, request.getFromUserId(), request.getToUserId());
        } else {
            request.setStatus(STATUS_REJECTED);
            log.info("好友请求已拒绝: requestId={}, fromUserId={}, toUserId={}", 
                    requestId, request.getFromUserId(), request.getToUserId());
        }
        
        friendRequestMapper.updateById(request);
        return true;
    }
    
    @Override
    public List<FriendRequestVO> getReceivedRequests(Long userId) {
        List<FriendRequest> requests = friendRequestMapper.selectReceivedRequests(userId, STATUS_PENDING);
        return convertToVOList(requests);
    }
    
    @Override
    public List<FriendRequestVO> getSentRequests(Long userId) {
        List<FriendRequest> requests = friendRequestMapper.selectSentRequests(userId, STATUS_PENDING);
        return convertToVOList(requests);
    }
    
    @Override
    public int getPendingCount(Long userId) {
        return friendRequestMapper.countByFromAndTo(null, userId, STATUS_PENDING);
    }
    
    @Override
    public boolean cancelRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            log.warn("好友请求不存在: requestId={}", requestId);
            return false;
        }
        
        if (!request.getFromUserId().equals(userId)) {
            log.warn("无权取消此请求: userId={}, requestFromUserId={}", userId, request.getFromUserId());
            return false;
        }
        
        request.setStatus(STATUS_EXPIRED);
        friendRequestMapper.updateById(request);
        log.info("好友请求已取消: requestId={}", requestId);
        return true;
    }
    
    /**
     * 转换为VO列表
     */
    private List<FriendRequestVO> convertToVOList(List<FriendRequest> requests) {
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有发起者ID
        List<Long> fromUserIds = requests.stream()
                .map(FriendRequest::getFromUserId)
                .distinct()
                .toList();
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(fromUserIds);
        
        return requests.stream()
                .map(request -> {
                    User fromUser = users.stream()
                            .filter(u -> u.getId().equals(request.getFromUserId()))
                            .findFirst()
                            .orElse(null);
                    
                    return FriendRequestVO.builder()
                            .id(request.getId())
                            .fromUserId(request.getFromUserId())
                            .fromUsername(fromUser != null ? fromUser.getUsername() : "")
                            .fromRealName(fromUser != null ? fromUser.getRealName() : "")
                            .fromAvatar(fromUser != null ? fromUser.getAvatar() : "")
                            .remark(request.getRemark())
                            .status(request.getStatus())
                            .statusDesc(getStatusDesc(request.getStatus()))
                            .expiresTime(request.getExpiresTime())
                            .createTime(request.getCreateTime())
                            .build();
                })
                .toList();
    }
    
    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "已同意";
            case 2 -> "已拒绝";
            case 3 -> "已过期";
            default -> "未知";
        };
    }
}