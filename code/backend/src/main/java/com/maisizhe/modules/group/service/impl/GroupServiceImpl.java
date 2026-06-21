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
        // 手动设置属性，避免 BeanUtils.copyProperties 无法处理字段名不一致的问题
        group.setGroupName(dto.getGroupName());
        group.setGroupAvatar(dto.getGroupAvatar());
        group.setGroupType(dto.getGroupType());
        group.setClassNo(dto.getClassNo());
        group.setCourseId(dto.getCourseCode() != null ? Long.parseLong(dto.getCourseCode()) : null);
        group.setMaxMembers(dto.getMaxMembers() != null ? dto.getMaxMembers() : 500);
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
        member.setQuitTime(null); // 未退群
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
                .eq(GroupMember::getUserId, userId) // 查询该用户的群组
                .isNull(GroupMember::getQuitTime) // quitTime为null表示未退群
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
            if (existingMember.getQuitTime() == null) {
                throw new BusinessException("已是群成员");
            }
            // 重新激活
            existingMember.setQuitTime(null);
            existingMember.setJoinTime(LocalDateTime.now());
            groupMemberMapper.updateById(existingMember);
        } else {
            // 3. 添加新成员
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole(MemberRoleEnum.MEMBER.getCode());
            member.setJoinTime(LocalDateTime.now());
            member.setQuitTime(null); // 未退群
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
        
        if (member == null || member.getQuitTime() != null) {
            throw new BusinessException("不是群成员");
        }
        
        // 2. 群主不能退群(只能转让)
        if (member.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new BusinessException("群主不能退群，请先转让群主身份");
        }
        
        // 3. 标记为已退群
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
        // 1. 查询群成员列表
        List<GroupMember> members = groupMemberMapper.selectList(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .isNull(GroupMember::getQuitTime) // quitTime为null表示未退群
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
        
        if (targetMember == null || targetMember.getQuitTime() != null) {
            throw new BusinessException("不是群成员");
        }
        
        if (targetMember.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new BusinessException("不能踢出群主");
        }
        
        // 3. 标记为已退群
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
     * 解散群组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disbandGroup(Long groupId, Long operatorId) {
        // 1. 查询群组
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 2. 检查群主权限
        if (!group.getCreatorId().equals(operatorId)) {
            throw new BusinessException("只有群主可以解散群组");
        }
        
        // 3. 标记群组为已解散
        group.setIsDisbanded(1);
        group.setDisbandTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("解散群组: groupId={}, operatorId={}", groupId, operatorId);
    }
    
    /**
     * 转让群主身份
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferOwnership(Long groupId, Long fromUid, Long toUid) {
        // 1. 验证当前群主身份
        GroupMember fromMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, fromUid)
                .isNull(GroupMember::getQuitTime)
        );
        
        if (fromMember == null) {
            throw new BusinessException("您不是群成员");
        }
        
        if (!fromMember.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new BusinessException("只有群主可以转让群主身份");
        }
        
        // 2. 验证新群主是否是群成员
        GroupMember toMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, toUid)
                .isNull(GroupMember::getQuitTime)
        );
        
        if (toMember == null) {
            throw new BusinessException("被转让者不是群成员");
        }
        
        // 3. 更新角色
        fromMember.setRole(MemberRoleEnum.MEMBER.getCode());
        groupMemberMapper.updateById(fromMember);
        
        toMember.setRole(MemberRoleEnum.OWNER.getCode());
        groupMemberMapper.updateById(toMember);
        
        // 4. 更新群组的creatorId
        Group group = groupMapper.selectById(groupId);
        group.setCreatorId(toUid);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("转让群主身份: groupId={}, fromUid={}, toUid={}", groupId, fromUid, toUid);
    }
    
    /**
     * 禁言成员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteMember(Long groupId, Long targetUserId, int muteMinutes, Long operatorId) {
        // 1. 检查操作者权限(管理员或群主)
        checkPermission(groupId, operatorId, true);
        
        // 2. 不能禁言自己
        if (targetUserId.equals(operatorId)) {
            throw new BusinessException("不能禁言自己");
        }
        
        // 3. 查询目标成员
        GroupMember targetMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, targetUserId)
                .isNull(GroupMember::getQuitTime)
        );
        
        if (targetMember == null) {
            throw new BusinessException("目标用户不是群成员");
        }
        
        // 4. 不能禁言群主
        if (targetMember.getRole().equals(MemberRoleEnum.OWNER.getCode())) {
            throw new BusinessException("不能禁言群主");
        }
        
        // 5. 设置禁言时间
        LocalDateTime muteUntil;
        if (muteMinutes < 0) {
            // 永久禁言
            muteUntil = LocalDateTime.now().plusYears(100);
        } else {
            muteUntil = LocalDateTime.now().plusMinutes(muteMinutes);
        }
        
        targetMember.setMuteUntil(muteUntil);
        groupMemberMapper.updateById(targetMember);
        
        log.info("禁言成员: groupId={}, targetUserId={}, muteMinutes={}, operatorId={}", 
            groupId, targetUserId, muteMinutes, operatorId);
    }
    
    /**
     * 解除禁言
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unmuteMember(Long groupId, Long targetUserId, Long operatorId) {
        // 1. 检查操作者权限(管理员或群主)
        checkPermission(groupId, operatorId, true);
        
        // 2. 查询目标成员
        GroupMember targetMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, targetUserId)
                .isNull(GroupMember::getQuitTime)
        );
        
        if (targetMember == null) {
            throw new BusinessException("目标用户不是群成员");
        }
        
        // 3. 解除禁言
        targetMember.setMuteUntil(null);
        groupMemberMapper.updateById(targetMember);
        
        log.info("解除禁言: groupId={}, targetUserId={}, operatorId={}", 
            groupId, targetUserId, operatorId);
    }
    
    /**
     * 获取群组成员ID列表
     */
    @Override
    public List<Long> getGroupMemberIds(Long groupId) {
        List<GroupMember> members = groupMemberMapper.selectList(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .isNull(GroupMember::getQuitTime)
        );
        
        return members.stream()
            .map(GroupMember::getUserId)
            .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否被禁言
     */
    @Override
    public boolean isMemberMuted(Long groupId, Long userId) {
        GroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId)
                .isNull(GroupMember::getQuitTime)
        );
        
        if (member == null || member.getMuteUntil() == null) {
            return false;
        }
        
        return member.getMuteUntil().isAfter(LocalDateTime.now());
    }
    
    /**
     * 邀请用户加入群组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteMember(Long groupId, Long targetUserId, Long operatorId) {
        // 1. 检查操作者权限(管理员或群主)
        checkPermission(groupId, operatorId, true);
        
        // 2. 检查目标用户是否已在群中
        GroupMember existingMember = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, targetUserId)
        );
        
        if (existingMember != null && existingMember.getQuitTime() == null) {
            throw new BusinessException("用户已在群中");
        }
        
        // 3. 创建群成员记录（直接加入，不需要验证）
        GroupMember newMember = GroupMember.builder()
            .groupId(groupId)
            .userId(targetUserId)
            .role(MemberRoleEnum.MEMBER.getCode())
            .joinTime(LocalDateTime.now())
            .build();
        
        groupMemberMapper.insert(newMember);
        
        // 4. 更新群成员数
        Group group = groupMapper.selectById(groupId);
        if (group != null) {
            group.setMemberCount(group.getMemberCount() + 1);
            group.setUpdateTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        
        log.info("邀请成员成功: groupId={}, targetUserId={}, operatorId={}", 
            groupId, targetUserId, operatorId);
    }
    
    /**
     * 批量邀请用户加入群组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteMembers(Long groupId, List<Long> userIds, Long operatorId) {
        // 1. 检查操作者权限(管理员或群主)
        checkPermission(groupId, operatorId, true);
        
        // 2. 过滤已在群中的用户
        List<GroupMember> existingMembers = groupMemberMapper.selectList(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getUserId, userIds)
                .isNull(GroupMember::getQuitTime)
        );
        
        List<Long> existingUserIds = existingMembers.stream()
            .map(GroupMember::getUserId)
            .collect(Collectors.toList());
        
        List<Long> newUserIds = userIds.stream()
            .filter(id -> !existingUserIds.contains(id))
            .collect(Collectors.toList());
        
        // 3. 批量添加新成员（inviterId为操作者，即邀请人）
        if (!newUserIds.isEmpty()) {
            List<GroupMember> newMembers = newUserIds.stream()
                .map(uid -> GroupMember.builder()
                    .groupId(groupId)
                    .userId(uid)
                    .inviterId(operatorId)
                    .role(MemberRoleEnum.MEMBER.getCode())
                    .joinTime(LocalDateTime.now())
                    .build())
                .collect(Collectors.toList());
            
            groupMemberMapper.insertBatchSomeColumn(newMembers);
            
            // 4. 更新群成员数
            Group group = groupMapper.selectById(groupId);
            if (group != null) {
                group.setMemberCount(group.getMemberCount() + newMembers.size());
                group.setUpdateTime(LocalDateTime.now());
                groupMapper.updateById(group);
            }
        }
        
        log.info("批量邀请成员成功: groupId={}, count={}, operatorId={}", 
            groupId, newUserIds.size(), operatorId);
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
                .isNull(GroupMember::getQuitTime) // quitTime为null表示未退群
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
                .isNull(GroupMember::getQuitTime) // quitTime为null表示未退群
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
        vo.setGroupNickname(member.getGroupNickname());
        vo.setJoinTime(member.getJoinTime());
        vo.setInviterId(member.getInviterId());
        
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
        
        // 检查是否被禁言
        if (member.getMuteUntil() != null && member.getMuteUntil().isAfter(LocalDateTime.now())) {
            vo.setIsMuted(true);
            vo.setMuteUntil(member.getMuteUntil());
        } else {
            vo.setIsMuted(false);
        }
        
        // 检查是否设置了免打扰
        vo.setIsSelfMuted(member.getIsMuted() != null && member.getIsMuted() == 1);
        
        // 如果有邀请人，获取邀请人姓名
        if (member.getInviterId() != null) {
            User inviter = userMapper.selectById(member.getInviterId());
            if (inviter != null) {
                vo.setInviterName(inviter.getRealName() != null ? inviter.getRealName() : inviter.getUsername());
            }
        }
        
        return vo;
    }
}
