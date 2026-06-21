package com.maisizhe.modules.friend.controller;

import com.maisizhe.common.response.Result;
import com.maisizhe.modules.friend.dto.AddFriendDTO;
import com.maisizhe.modules.friend.dto.UpdateFriendRemarkDTO;
import com.maisizhe.modules.friend.dto.UpdateFriendSortDTO;
import com.maisizhe.modules.friend.service.FriendRequestService;
import com.maisizhe.modules.friend.service.FriendService;
import com.maisizhe.modules.friend.vo.FriendRequestVO;
import com.maisizhe.modules.friend.vo.FriendVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友控制器
 * 处理好友管理相关接口
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {
    
    private final FriendService friendService;
    private final FriendRequestService friendRequestService;
    
    /**
     * 获取好友列表
     * 从JWT Token中获取当前用户ID
     * 
     * @return 好友列表
     */
    @GetMapping("/list")
    public Result<List<FriendVO>> getFriends() {
        Long userId = getCurrentUserId();
        List<FriendVO> friends = friendService.getFriends(userId);
        return Result.success(friends);
    }
    
    /**
     * 搜索用户
     * 
     * @param keyword 关键词(用户名/学号/姓名)
     * @return 用户列表
     */
    @GetMapping("/search")
    public Result<List<FriendVO>> searchUsers(@RequestParam String keyword) {
        Long userId = getCurrentUserId();
        List<FriendVO> users = friendService.searchUsers(keyword, userId);
        return Result.success(users);
    }
    
    /**
     * 获取好友详情
     * 
     * @param friendId 好友ID
     * @return 好友详情
     */
    @GetMapping("/{friendId}")
    public Result<FriendVO> getFriendDetail(@PathVariable Long friendId) {
        Long userId = getCurrentUserId();
        FriendVO friend = friendService.getFriendDetail(userId, friendId);
        if (friend == null) {
            return Result.error(404, "好友不存在");
        }
        return Result.success(friend);
    }
    
    /**
     * 添加好友
     * 
     * @param dto 添加好友请求
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> addFriend(@Valid @RequestBody AddFriendDTO dto) {
        Long userId = getCurrentUserId();
        boolean success = friendService.addFriend(userId, dto);
        if (!success) {
            return Result.error(400, "添加好友失败");
        }
        return Result.success();
    }
    
    /**
     * 删除好友
     * 
     * @param friendId 好友ID
     * @return 操作结果
     */
    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long friendId) {
        Long userId = getCurrentUserId();
        boolean success = friendService.deleteFriend(userId, friendId);
        if (!success) {
            return Result.error(400, "删除好友失败");
        }
        return Result.success();
    }
    
    /**
     * 更新好友备注
     * 
     * @param dto 更新备注请求
     * @return 操作结果
     */
    @PutMapping("/remark")
    public Result<Void> updateRemark(@Valid @RequestBody UpdateFriendRemarkDTO dto) {
        Long userId = getCurrentUserId();
        boolean success = friendService.updateRemark(userId, dto);
        if (!success) {
            return Result.error(400, "更新备注失败");
        }
        return Result.success();
    }
    
    /**
     * 更新好友排序/置顶状态
     * 
     * @param dto 更新排序请求
     * @return 操作结果
     */
    @PutMapping("/sort")
    public Result<Void> updateSort(@Valid @RequestBody UpdateFriendSortDTO dto) {
        Long userId = getCurrentUserId();
        boolean success = friendService.updateSort(userId, dto);
        if (!success) {
            return Result.error(400, "更新排序失败");
        }
        return Result.success();
    }
    
    /**
     * 批量更新好友排序
     * 
     * @param sortList 排序列表
     * @return 操作结果
     */
    @PutMapping("/batch-sort")
    public Result<Void> batchUpdateSort(@RequestBody List<UpdateFriendSortDTO> sortList) {
        Long userId = getCurrentUserId();
        boolean success = friendService.batchUpdateSort(userId, sortList);
        if (!success) {
            return Result.error(400, "批量更新排序失败");
        }
        return Result.success();
    }
    
    /**
     * 检查是否为好友关系
     * 
     * @param friendId 好友ID
     * @return 是否为好友
     */
    @GetMapping("/check/{friendId}")
    public Result<Boolean> checkFriend(@PathVariable Long friendId) {
        Long userId = getCurrentUserId();
        boolean isFriend = friendService.isFriend(userId, friendId);
        return Result.success(isFriend);
    }
    
    // ==================== 好友请求相关接口 ====================
    
    /**
     * 发送好友请求
     * 
     * @param dto 添加好友请求
     * @return 操作结果
     */
    @PostMapping("/request/send")
    public Result<Void> sendFriendRequest(@Valid @RequestBody AddFriendDTO dto) {
        Long userId = getCurrentUserId();
        boolean success = friendRequestService.sendRequest(userId, dto.getTargetUserId(), dto.getRemark());
        if (!success) {
            return Result.error(400, "发送好友请求失败，可能已存在请求或已是好友");
        }
        return Result.success();
    }
    
    /**
     * 处理好友请求（同意/拒绝）
     * 
     * @param requestId 请求ID
     * @param accept 是否同意
     * @return 操作结果
     */
    @PostMapping("/request/handle")
    public Result<Void> handleFriendRequest(
            @RequestParam Long requestId,
            @RequestParam Boolean accept) {
        Long userId = getCurrentUserId();
        boolean success = friendRequestService.handleRequest(requestId, userId, accept);
        if (!success) {
            return Result.error(400, "处理好友请求失败");
        }
        return Result.success();
    }
    
    /**
     * 获取收到的好友请求列表
     * 
     * @return 请求列表
     */
    @GetMapping("/request/received")
    public Result<List<FriendRequestVO>> getReceivedRequests() {
        Long userId = getCurrentUserId();
        List<FriendRequestVO> requests = friendRequestService.getReceivedRequests(userId);
        return Result.success(requests);
    }
    
    /**
     * 获取发送的好友请求列表
     * 
     * @return 请求列表
     */
    @GetMapping("/request/sent")
    public Result<List<FriendRequestVO>> getSentRequests() {
        Long userId = getCurrentUserId();
        List<FriendRequestVO> requests = friendRequestService.getSentRequests(userId);
        return Result.success(requests);
    }
    
    /**
     * 获取待处理的好友请求数量
     * 
     * @return 待处理数量
     */
    @GetMapping("/request/pending-count")
    public Result<Integer> getPendingRequestCount() {
        Long userId = getCurrentUserId();
        int count = friendRequestService.getPendingCount(userId);
        return Result.success(count);
    }
    
    /**
     * 取消好友请求
     * 
     * @param requestId 请求ID
     * @return 操作结果
     */
    @PostMapping("/request/cancel")
    public Result<Void> cancelFriendRequest(@RequestParam Long requestId) {
        Long userId = getCurrentUserId();
        boolean success = friendRequestService.cancelRequest(requestId, userId);
        if (!success) {
            return Result.error(400, "取消好友请求失败");
        }
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
