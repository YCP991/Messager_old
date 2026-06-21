package com.maisizhe.modules.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群成员实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("group_member")
public class GroupMember {
    
    /**
     * 成员ID(自增)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 群组ID
     */
    @TableField("group_id")
    private Long groupId;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 邀请人ID(NULL表示自己加入)
     */
    @TableField("inviter_id")
    private Long inviterId;
    
    /**
     * 成员角色:0-普通成员,1-管理员,2-群主
     */
    private Integer role;
    
    /**
     * 群昵称(覆盖全局昵称)
     */
    @TableField("group_nickname")
    private String groupNickname;
    
    /**
     * 是否置顶该群:0-否,1-是
     */
    @TableField("is_pinned")
    private Integer isPinned;
    
    /**
     * 是否免打扰:0-否,1-是
     */
    @TableField("is_muted")
    private Integer isMuted;
    
    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Double sortOrder;
    
    /**
     * 禁言截止时间(NULL表示不禁言)
     */
    @TableField("mute_until")
    private LocalDateTime muteUntil;
    
    /**
     * 入群时间
     */
    @TableField("join_time")
    private LocalDateTime joinTime;
    
    /**
     * 退群时间(NULL表示在群)
     */
    @TableField("quit_time")
    private LocalDateTime quitTime;
    
    /**
     * 逻辑删除:0-未删除,1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}
