package com.maisizhe.modules.group.controller;

import com.maisizhe.common.response.Result;
import com.maisizhe.modules.group.dto.CreateGroupDTO;
import com.maisizhe.modules.group.service.GroupService;
import com.maisizhe.modules.group.vo.GroupMemberVO;
import com.maisizhe.modules.group.vo.GroupVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组控制器
 * 处理群组管理相关接口
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {
    
    private final GroupService groupService;
    
    /**
     * 创建群组
     * 
     * @param dto 创建请求
     * @return 群组信息
     */
    @PostMapping("/create")
    public Result<GroupVO> createGroup(@Valid @RequestBody CreateGroupDTO dto) {
        Long creatorId = getCurrentUserId();
        log.info("创建群组: creatorId={}, groupName={}", creatorId, dto.getGroupName());
        GroupVO groupVO = groupService.createGroup(creatorId, dto);
        return Result.success(groupVO);
    }
    
    /**
     * 获取群组详情
     * 
     * @param groupId 群组ID
     * @return 群组信息
     */
    @GetMapping("/{groupId}")
    public Result<GroupVO> getGroupById(@PathVariable Long groupId) {
        Long userId = getCurrentUserId();
        GroupVO groupVO = groupService.getGroupById(groupId, userId);
        return Result.success(groupVO);
    }
    
    /**
     * 获取用户加入的群组列表
     * 从JWT Token中获取当前用户ID
     * 
     * @return 群组列表
     */
    @GetMapping("/my-groups")
    public Result<List<GroupVO>> getUserGroups() {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Result.error(401, "未授权");
        }
        
        Long userId = (Long) authentication.getPrincipal();
        List<GroupVO> groups = groupService.getUserGroups(userId);
        return Result.success(groups);
    }
    
    /**
     * 加入群组
     * 
     * @param groupId 群组ID
     * @return 成功响应
     */
    @PostMapping("/{groupId}/join")
    public Result<Void> joinGroup(@PathVariable Long groupId) {
        Long userId = getCurrentUserId();
        log.info("加入群组: groupId={}, userId={}", groupId, userId);
        groupService.joinGroup(groupId, userId);
        return Result.success();
    }
    
    /**
     * 退出群组
     * 
     * @param groupId 群组ID
     * @return 成功响应
     */
    @PostMapping("/{groupId}/quit")
    public Result<Void> quitGroup(@PathVariable Long groupId) {
        Long userId = getCurrentUserId();
        log.info("退出群组: groupId={}, userId={}", groupId, userId);
        groupService.quitGroup(groupId, userId);
        return Result.success();
    }
    
    /**
     * 获取群成员列表
     * 
     * @param groupId 群组ID
     * @return 成员列表
     */
    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberVO>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberVO> members = groupService.getGroupMembers(groupId);
        return Result.success(members);
    }
    
    /**
     * 踢出群成员
     * 
     * @param groupId 群组ID
     * @param targetUserId 目标用户ID
     * @return 成功响应
     */
    @PostMapping("/{groupId}/kick")
    public Result<Void> kickMember(
            @PathVariable Long groupId,
            @RequestParam Long targetUserId) {
        Long operatorId = getCurrentUserId();
        log.info("踢出群成员: groupId={}, targetUserId={}, operatorId={}", 
            groupId, targetUserId, operatorId);
        groupService.kickMember(groupId, targetUserId, operatorId);
        return Result.success();
    }
    
    /**
     * 更新群组公告
     * 
     * @param groupId 群组ID
     * @param announcement 新公告
     * @return 成功响应
     */
    @PutMapping("/{groupId}/announcement")
    public Result<Void> updateAnnouncement(
            @PathVariable Long groupId,
            @RequestParam String announcement) {
        Long operatorId = getCurrentUserId();
        log.info("更新群组公告: groupId={}, operatorId={}", groupId, operatorId);
        groupService.updateAnnouncement(groupId, announcement, operatorId);
        return Result.success();
    }
    
    /**
     * 解散群组（仅群主可操作）
     * 
     * @param groupId 群组ID
     * @return 成功响应
     */
    @PostMapping("/{groupId}/disband")
    public Result<Void> disbandGroup(@PathVariable Long groupId) {
        Long operatorId = getCurrentUserId();
        log.info("解散群组: groupId={}, operatorId={}", groupId, operatorId);
        groupService.disbandGroup(groupId, operatorId);
        return Result.success();
    }
    
    /**
     * 转让群主身份
     * 
     * @param groupId 群组ID
     * @param toUid 新群主ID
     * @return 成功响应
     */
    @PostMapping("/{groupId}/transfer")
    public Result<Void> transferOwnership(
            @PathVariable Long groupId,
            @RequestParam Long toUid) {
        Long fromUid = getCurrentUserId();
        log.info("转让群主身份: groupId={}, fromUid={}, toUid={}", groupId, fromUid, toUid);
        groupService.transferOwnership(groupId, fromUid, toUid);
        return Result.success();
    }
    
    /**
     * 禁言成员
     * 
     * @param groupId 群组ID
     * @param targetUserId 目标用户ID
     * @param muteMinutes 禁言时长（分钟），-1表示永久禁言
     * @return 成功响应
     */
    @PostMapping("/{groupId}/mute")
    public Result<Void> muteMember(
            @PathVariable Long groupId,
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "30") int muteMinutes) {
        Long operatorId = getCurrentUserId();
        log.info("禁言成员: groupId={}, targetUserId={}, muteMinutes={}, operatorId={}", 
            groupId, targetUserId, muteMinutes, operatorId);
        groupService.muteMember(groupId, targetUserId, muteMinutes, operatorId);
        return Result.success();
    }
    
    /**
     * 解除禁言
     * 
     * @param groupId 群组ID
     * @param targetUserId 目标用户ID
     * @return 成功响应
     */
    @PostMapping("/{groupId}/unmute")
    public Result<Void> unmuteMember(
            @PathVariable Long groupId,
            @RequestParam Long targetUserId) {
        Long operatorId = getCurrentUserId();
        log.info("解除禁言: groupId={}, targetUserId={}, operatorId={}", 
            groupId, targetUserId, operatorId);
        groupService.unmuteMember(groupId, targetUserId, operatorId);
        return Result.success();
    }
    
    /**
     * 邀请用户加入群组（管理员或群主）
     * 
     * @param groupId 群组ID
     * @param targetUserId 目标用户ID
     * @return 成功响应
     */
    @PostMapping("/{groupId}/invite")
    public Result<Void> inviteMember(
            @PathVariable Long groupId,
            @RequestParam Long targetUserId) {
        Long operatorId = getCurrentUserId();
        log.info("邀请成员: groupId={}, targetUserId={}, operatorId={}", 
            groupId, targetUserId, operatorId);
        groupService.inviteMember(groupId, targetUserId, operatorId);
        return Result.success();
    }
    
    /**
     * 批量邀请用户加入群组
     * 
     * @param groupId 群组ID
     * @param userIds 用户ID列表（逗号分隔）
     * @return 成功响应
     */
    @PostMapping("/{groupId}/invite/batch")
    public Result<Void> inviteMembers(
            @PathVariable Long groupId,
            @RequestParam List<Long> userIds) {
        Long operatorId = getCurrentUserId();
        log.info("批量邀请成员: groupId={}, userIds={}, operatorId={}", 
            groupId, userIds, operatorId);
        groupService.inviteMembers(groupId, userIds, operatorId);
        return Result.success();
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("用户未登录");
        }
        return (Long) authentication.getPrincipal();
    }
}
