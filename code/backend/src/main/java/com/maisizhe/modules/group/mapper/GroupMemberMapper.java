package com.maisizhe.modules.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maisizhe.modules.group.entity.GroupMember;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 群成员Mapper接口
 * 
 * @author MaiSiZhe Team
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    
    /**
     * 批量插入群成员
     * 
     * @param members 成员列表
     * @return 插入数量
     */
    @Insert("<script>" +
            "INSERT INTO group_member (group_id, user_id, inviter_id, role, is_pinned, is_muted, sort_order, join_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.groupId}, #{item.userId}, #{item.inviterId}, #{item.role}, #{item.isPinned}, #{item.isMuted}, #{item.sortOrder}, #{item.joinTime})" +
            "</foreach>" +
            "</script>")
    int insertBatchSomeColumn(List<GroupMember> members);
}
