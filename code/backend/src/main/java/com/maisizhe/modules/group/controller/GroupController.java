package com.maisizhe.modules.group.controller;

import com.maisizhe.common.response.Result;
import com.maisizhe.modules.group.dto.CreateGroupDTO;
import com.maisizhe.modules.group.service.GroupService;
import com.maisizhe.modules.group.vo.GroupMemberVO;
import com.maisizhe.modules.group.vo.GroupVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @param creatorId 创建者ID(从JWT中获取)
     * @param dto 创建请求
     * @return 群组信息
     */
    @PostMapping("/create")
    public Result<GroupVO> createGroup(
            @RequestParam Long creatorId,
            @Valid @RequestBody CreateGroupDTO dto) {
        log.info("创建群组: creatorId={}, groupName={}", creatorId, dto.getGroupName());
        GroupVO groupVO = groupService.createGroup(creatorId, dto);
        return Result.success(groupVO);
    }
    
    /**
     * 获取群组详情
     * 
     * @param groupId 群组ID
     * @param userId 当前用户ID(从JWT中获取)
     * @return 群组信息
     */
    @GetMapping("/{groupId}")
    public Result<GroupVO> getGroupById(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        GroupVO groupVO = groupService.getGroupById(groupId, userId);
        return Result.success(groupVO);
    }
    
    /**
     * 获取用户加入的群组列表
     * 
     * @param userId 用户ID(从JWT中获取)
     * @return 群组列表
     */
    @GetMapping("/my-groups")
    public Result<List<GroupVO>> getUserGroups(@RequestParam Long userId) {
        List<GroupVO> groups = groupService.getUserGroups(userId);
        return Result.success(groups);
    }
    
    /**
     * 加入群组
     * 
     * @param groupId 群组ID
     * @param userId 用户ID(从JWT中获取)
     * @return 成功响应
     */
    @PostMapping("/{groupId}/join")
    public Result<Void> joinGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        log.info("加入群组: groupId={}, userId={}", groupId, userId);
        groupService.joinGroup(groupId, userId);
        return Result.success();
    }
    
    /**
     * 退出群组
     * 
     * @param groupId 群组ID
     * @param userId 用户ID(从JWT中获取)
     * @return 成功响应
     */
    @PostMapping("/{groupId}/quit")
    public Result<Void> quitGroup(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
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
     * @param operatorId 操作用户ID(从JWT中获取)
     * @return 成功响应
     */
    @PostMapping("/{groupId}/kick")
    public Result<Void> kickMember(
            @PathVariable Long groupId,
            @RequestParam Long targetUserId,
            @RequestParam Long operatorId) {
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
     * @param operatorId 操作用户ID(从JWT中获取)
     * @return 成功响应
     */
    @PutMapping("/{groupId}/announcement")
    public Result<Void> updateAnnouncement(
            @PathVariable Long groupId,
            @RequestParam String announcement,
            @RequestParam Long operatorId) {
        log.info("更新群组公告: groupId={}, operatorId={}", groupId, operatorId);
        groupService.updateAnnouncement(groupId, announcement, operatorId);
        return Result.success();
    }
}
