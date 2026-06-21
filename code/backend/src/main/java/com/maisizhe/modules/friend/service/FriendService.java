package com.maisizhe.modules.friend.service;

import com.maisizhe.modules.friend.dto.AddFriendDTO;
import com.maisizhe.modules.friend.dto.UpdateFriendRemarkDTO;
import com.maisizhe.modules.friend.dto.UpdateFriendSortDTO;
import com.maisizhe.modules.friend.vo.FriendVO;

import java.util.List;

/**
 * 好友服务接口
 * 
 * @author MaiSiZhe Team
 */
public interface FriendService {
    
    /**
     * 获取用户的好友列表
     * 
     * @param userId 用户ID
     * @return 好友VO列表
     */
    List<FriendVO> getFriends(Long userId);
    
    /**
     * 添加好友
     * 
     * @param userId 当前用户ID
     * @param dto 添加好友请求
     * @return 操作结果
     */
    boolean addFriend(Long userId, AddFriendDTO dto);
    
    /**
     * 删除好友
     * 
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 操作结果
     */
    boolean deleteFriend(Long userId, Long friendId);
    
    /**
     * 更新好友备注
     * 
     * @param userId 当前用户ID
     * @param dto 更新备注请求
     * @return 操作结果
     */
    boolean updateRemark(Long userId, UpdateFriendRemarkDTO dto);
    
    /**
     * 更新好友排序/置顶状态
     * 
     * @param userId 当前用户ID
     * @param dto 更新排序请求
     * @return 操作结果
     */
    boolean updateSort(Long userId, UpdateFriendSortDTO dto);
    
    /**
     * 批量更新好友排序
     * 
     * @param userId 当前用户ID
     * @param sortList 排序列表 [(friendId, sortOrder, isPinned), ...]
     * @return 操作结果
     */
    boolean batchUpdateSort(Long userId, List<UpdateFriendSortDTO> sortList);
    
    /**
     * 搜索用户(用于添加好友)
     * 
     * @param keyword 关键词(用户名/学号/姓名)
     * @param excludeUserId 排除的用户ID(当前用户)
     * @return 用户列表
     */
    List<FriendVO> searchUsers(String keyword, Long excludeUserId);
    
    /**
     * 获取好友详情
     * 
     * @param userId 当前用户ID
     * @param friendId 好友ID
     * @return 好友VO
     */
    FriendVO getFriendDetail(Long userId, Long friendId);
    
    /**
     * 检查两个用户是否为好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return true-是好友, false-不是好友
     */
    boolean isFriend(Long userId, Long friendId);
}
