package com.maisizhe.modules.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maisizhe.modules.friend.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友 Mapper 接口
 * 
 * @author MaiSiZhe Team
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
    
    /**
     * 查询用户的好友列表
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    @Select("SELECT friend_id FROM friend WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectFriendIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询两个用户是否为好友关系
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友记录数
     */
    @Select("SELECT COUNT(*) FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND deleted = 0")
    int selectFriendCount(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    /**
     * 根据用户ID和好友ID查询好友记录
     * 
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友记录
     */
    @Select("SELECT * FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND deleted = 0")
    Friend selectByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
