package com.maisizhe.modules.group.service;

import com.maisizhe.modules.group.dto.CreateGroupDTO;
import com.maisizhe.modules.group.vo.GroupMemberVO;
import com.maisizhe.modules.group.vo.GroupVO;

import java.util.List;

/**
 * 群组服务接口
 * 
 * @author MaiSiZhe Team
 */
public interface GroupService {
    
    /**
     * 创建群组
     * 
     * @param creatorId 创建者ID
     * @param dto 创建请求
     * @return 群组信息
     */
    GroupVO createGroup(Long creatorId, CreateGroupDTO dto);
    
    /**
     * 获取群组详情
     * 
     * @param groupId 群组ID
     * @param userId 当前用户ID
     * @return 群组信息
     */
    GroupVO getGroupById(Long groupId, Long userId);
    
    /**
     * 获取用户加入的群组列表
     * 
     * @param userId 用户ID
     * @return 群组列表
     */
    List<GroupVO> getUserGroups(Long userId);
    
    /**
     * 加入群组
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     */
    void joinGroup(Long groupId, Long userId);
    
    /**
     * 退出群组
     * 
     * @param groupId 群组ID
     * @param userId 用户ID
     */
    void quitGroup(Long groupId, Long userId);
    
    /**
     * 获取群成员列表
     * 
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<GroupMemberVO> getGroupMembers(Long groupId);
    
    /**
     * 踢出群成员
     * 
     * @param groupId 群组ID
     * @param targetUserId 目标用户ID
     * @param operatorId 操作用户ID(管理员或群主)
     */
    void kickMember(Long groupId, Long targetUserId, Long operatorId);
    
    /**
     * 更新群组公告
     * 
     * @param groupId 群组ID
     * @param announcement 新公告
     * @param operatorId 操作用户ID
     */
    void updateAnnouncement(Long groupId, String announcement, Long operatorId);
}
