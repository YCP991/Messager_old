package com.maisizhe.modules.friend.service;

import com.maisizhe.modules.friend.vo.FriendRequestVO;

import java.util.List;

/**
 * 好友请求服务接口
 * 
 * @author MaiSiZhe Team
 */
public interface FriendRequestService {
    
    /**
     * 发送好友请求
     * 
     * @param fromUserId 发起者ID
     * @param toUserId 接收者ID
     * @param remark 备注/留言
     * @return 是否成功
     */
    boolean sendRequest(Long fromUserId, Long toUserId, String remark);
    
    /**
     * 处理好友请求
     * 
     * @param requestId 请求ID
     * @param userId 当前用户ID（必须是接收者）
     * @param accept 是否同意
     * @return 是否成功
     */
    boolean handleRequest(Long requestId, Long userId, boolean accept);
    
    /**
     * 获取用户收到的好友请求列表
     * 
     * @param userId 用户ID
     * @return 请求列表
     */
    List<FriendRequestVO> getReceivedRequests(Long userId);
    
    /**
     * 获取用户发送的好友请求列表
     * 
     * @param userId 用户ID
     * @return 请求列表
     */
    List<FriendRequestVO> getSentRequests(Long userId);
    
    /**
     * 获取待处理的请求数量
     * 
     * @param userId 用户ID
     * @return 待处理数量
     */
    int getPendingCount(Long userId);
    
    /**
     * 取消好友请求
     * 
     * @param requestId 请求ID
     * @param userId 当前用户ID（必须是发起者）
     * @return 是否成功
     */
    boolean cancelRequest(Long requestId, Long userId);
}