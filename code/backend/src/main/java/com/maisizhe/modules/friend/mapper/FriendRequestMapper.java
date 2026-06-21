package com.maisizhe.modules.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maisizhe.modules.friend.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友请求Mapper
 * 
 * @author MaiSiZhe Team
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    
    /**
     * 查询用户收到的好友请求列表
     * 
     * @param toUserId 接收者ID
     * @param status 状态筛选（可选）
     * @return 请求列表
     */
    @Select("SELECT * FROM friend_request WHERE to_user_id = #{toUserId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<FriendRequest> selectReceivedRequests(@Param("toUserId") Long toUserId, @Param("status") Integer status);
    
    /**
     * 查询用户发送的好友请求列表
     * 
     * @param fromUserId 发起者ID
     * @param status 状态筛选（可选）
     * @return 请求列表
     */
    @Select("SELECT * FROM friend_request WHERE from_user_id = #{fromUserId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<FriendRequest> selectSentRequests(@Param("fromUserId") Long fromUserId, @Param("status") Integer status);
    
    /**
     * 检查是否存在相同的好友请求
     * 
     * @param fromUserId 发起者ID
     * @param toUserId 接收者ID
     * @param status 状态
     * @return 请求记录数
     */
    @Select("SELECT COUNT(*) FROM friend_request WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND status = #{status} AND deleted = 0")
    int countByFromAndTo(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId, @Param("status") Integer status);
}