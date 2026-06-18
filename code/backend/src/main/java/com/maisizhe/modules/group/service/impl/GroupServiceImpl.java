package com.maisizhe.modules.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maisizhe.common.enums.GroupTypeEnum;
import com.maisizhe.common.enums.MemberRoleEnum;
import com.maisizhe.common.exception.BusinessException;
import com.maisizhe.modules.group.dto.CreateGroupDTO;
import com.maisizhe.modules.group.entity.Group;
import com.maisizhe.modules.group.entity.GroupMember;
import com.maisizhe.modules.group.mapper.GroupMapper;
import com.maisizhe.modules.group.mapper.GroupMemberMapper;
import com.maisizhe.modules.group.service.GroupService;
import com.maisizhe.modules.group.vo.GroupMemberVO;
import com.maisizhe.modules.group.vo.GroupVO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import com.maisizhe.websocket.session.UserSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群组服务实现类
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;
    private final UserSessionManager sessionManager;
    
    /**
     * 创建群组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupVO createGroup(Long creatorId, CreateGroupDTO dto) {
        // 1. 参数校验
        validateCreateGroup(dto);
        
        // 2. 创建群组
        Group group = new Group();
        BeanUtils.copyProperties(dto, group);
        group.setCreatorId(creatorId);
        group.setMemberCount(1); // 创建者自己
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.insert(group);
        
        // 3. 添加创建者为群主
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(creatorId);
        member.setRole(MemberRoleEnum.OWNER.getCode());
        member.setJoinTime(LocalDateTime.now());
        member.setIsQuit(0);
        groupMemberMapper.insert(member);
        
        log.info("创建群组成功: groupId={}, groupName={}, creatorId={}", 
            group.getId(), group.getGroupName(), creatorId);
        
        return convertToVO(group, MemberRoleEnum.OWNER.getCode());
    }
    
    /**
     * 获取群组详情
     */
    @Override
    public GroupVO getGroupById(Long groupId, Long userId) {
        // 1. 查询群组
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 2. 查询用户在群中的角色
        Integer myRole = getUserRoleInGroup(groupId, userId);
        
        return convertToVO(group, myRole);
    }
    
    /**
     * 获取用户加入的群组列表
     */
    @Override
    public List<GroupVO> getUserGroups(Long userId) {
        // 1. 查询用户加入的所有群组ID
        List<Long> groupIds = groupMemberMapper.selectList(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getIsQuit, 0)
        ).stream().map(GroupMember::getGroupId).collect(Collectors.toList());
        
        if (groupIds.isEmpty()) {
            return List.of();
        }
        
        // 2. 批量查询群组信息
        List<Group> groups = groupMapper.selectBatchIds(groupIds);
        
        // 3. 转换为VO
        return groups.stream()
            .map(group -> {
                Integer myRole = getUserRoleInGroup(group.getId(), userId);
                return convertToVO(group, myRole);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 加入群组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(Long groupId, Long userId) {
        // 1. 查询群组
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 2. 检查是否已是成员
        GroupMember existingMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
        );
        
        if (existingMember != null) {
            if (existingMember.getIsQuit() == 0) {
                throw new BusinessException("已是群成员");
            }
            // 重新激活
            existingMember.setIsQuit(0);
            existingMember.setJoinTime(LocalDateTime.now());
            groupMemberMapper.updateById(existingMember);
        } else {
            // 3. 添加新成员
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole(MemberRoleEnum.MEMBER.getCode());
            member.setJoinTime(LocalDateTime.now());
            member.setIsQuit(0);
            groupMemberMapper.insert(member);
        }
        
        // 4. 更新群组成员数
        group.setMemberCount(group.getMemberCount() + 1);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("用户加入群组: groupId={}, userId={}", groupId, userId);
    }
    
    /**
     * 退出群组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long groupId, Long userId) {
        // 1. 查询成员信息
        GroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
        );
        
        if (member == null || member.getIsQuit() == 1) {
            throw new BusinessException("不是群成员");
        }
        
        // 2. 群主不能退群(只能转让)
        if (member.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new BusinessException("群主不能退群，请先转让群主身份");
        }
        
        // 3. 标记为已退群
        member.setIsQuit(1);
        member.setQuitTime(LocalDateTime.now());
        groupMemberMapper.updateById(member);
        
        // 4. 更新群组成员数
        Group group = groupMapper.selectById(groupId);
        if (group != null && group.getMemberCount() > 0) {
            group.setMemberCount(group.getMemberCount() - 1);
            group.setUpdateTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        
        log.info("用户退出群组: groupId={}, userId={}", groupId, userId);
    }
    
    /**
     * 获取群成员列表
     */
    @Override
    public List<GroupMemberVO> getGroupMembers(Long groupId) {
        // 1. 查询所有成员
        List<GroupMember> members = groupMemberMapper.selectList(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getIsQuit, 0)
                .orderByAsc(GroupMember::getRole)
                .orderByDesc(GroupMember::getJoinTime)
        );
        
        // 2. 填充用户信息
        return members.stream().map(member -> {
            User user = userMapper.selectById(member.getUserId());
            return convertToMemberVO(member, user);
        }).collect(Collectors.toList());
    }
    
    /**
     * 踢出群成员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void kickMember(Long groupId, Long targetUserId, Long operatorId) {
        // 1. 检查操作者权限
        checkPermission(groupId, operatorId, true);
        
        // 2. 不能踢群主
        GroupMember targetMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, targetUserId)
        );
        
        if (targetMember == null || targetMember.getIsQuit() == 1) {
            throw new BusinessException("不是群成员");
        }
        
        if (targetMember.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new BusinessException("不能踢出群主");
        }
        
        // 3. 标记为已退群
        targetMember.setIsQuit(1);
        targetMember.setQuitTime(LocalDateTime.now());
        groupMemberMapper.updateById(targetMember);
        
        // 4. 更新群组成员数
        Group group = groupMapper.selectById(groupId);
        if (group != null && group.getMemberCount() > 0) {
            group.setMemberCount(group.getMemberCount() - 1);
            group.setUpdateTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        
        log.info("踢出群成员: groupId={}, targetUserId={}, operatorId={}", 
            groupId, targetUserId, operatorId);
    }
    
    /**
     * 更新群组公告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnouncement(Long groupId, String announcement, Long operatorId) {
        // 1. 检查操作者权限(管理员或群主)
        checkPermission(groupId, operatorId, true);
        
        // 2. 更新公告
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }
        
        group.setAnnouncement(announcement);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("更新群组公告: groupId={}, operatorId={}", groupId, operatorId);
    }
    
    /**
     * 校验创建群组参数
     */
    private void validateCreateGroup(CreateGroupDTO dto) {
        GroupTypeEnum groupType = GroupTypeEnum.getByCode(dto.getGroupType());
        if (groupType == null) {
            throw new BusinessException("无效的群组类型");
        }
        
        // 班级群/课程群需要验证班级号
        if (groupType == GroupTypeEnum.CLASS || groupType == GroupTypeEnum.COURSE) {
            if (dto.getClassNo() == null || dto.getClassNo().isEmpty()) {
                throw new BusinessException("班级群/课程群必须填写班级号");
            }
        }
    }
    
    /**
     * 获取用户在群中的角色
     */
    private Integer getUserRoleInGroup(Long groupId, Long userId) {
        GroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .eq(GroupMember::getIsQuit, 0)
        );
        return member != null ? member.getRole() : null;
    }
    
    /**
     * 检查操作权限
     * 
     * @param groupId 群组ID
     * @param operatorId 操作用户ID
     * @param requireAdmin 是否需要管理员权限
     */
    private void checkPermission(Long groupId, Long operatorId, boolean requireAdmin) {
        GroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, operatorId)
                .eq(GroupMember::getIsQuit, 0)
        );
        
        if (member == null) {
            throw new BusinessException("不是群成员");
        }
        
        if (requireAdmin && !isAdminOrOwner(member.getRole())) {
            throw new BusinessException("权限不足，需要管理员或群主身份");
        }
    }
    
    /**
     * 判断是否是管理员或群主
     */
    private boolean isAdminOrOwner(Integer role) {
        return role.equals(MemberRoleEnum.ADMIN.getCode()) 
            || role.equals(MemberRoleEnum.OWNER.getCode());
    }
    
    /**
     * 转换为VO对象
     */
    private GroupVO convertToVO(Group group, Integer myRole) {
        GroupVO vo = new GroupVO();
        BeanUtils.copyProperties(group, vo);
        
        // 设置群组类型描述
        GroupTypeEnum typeEnum = GroupTypeEnum.getByCode(group.getGroupType());
        if (typeEnum != null) {
            vo.setGroupTypeDesc(typeEnum.getDescription());
        }
        
        // 设置创建者姓名
        User creator = userMapper.selectById(group.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getRealName());
        }
        
        vo.setMyRole(myRole);
        return vo;
    }
    
    /**
     * 转换为成员VO对象
     */
    private GroupMemberVO convertToMemberVO(GroupMember member, User user) {
        GroupMemberVO vo = new GroupMemberVO();
        vo.setId(member.getId());
        vo.setUserId(member.getUserId());
        vo.setRole(member.getRole());
        vo.setJoinTime(member.getJoinTime());
        
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setAvatar(user.getAvatar());
        }
        
        // 设置角色描述
        MemberRoleEnum roleEnum = MemberRoleEnum.getByCode(member.getRole());
        if (roleEnum != null) {
            vo.setRoleDesc(roleEnum.getDescription());
        }
        
        // 检查是否在线
        vo.setIsOnline(sessionManager.isOnline(member.getUserId()));
        
        return vo;
    }
}
