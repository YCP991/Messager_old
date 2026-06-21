package com.maisizhe.modules.friend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友请求实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("friend_request")
public class FriendRequest {
    
    /**
     * 请求ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 请求发起者ID
     */
    @TableField("from_user_id")
    private Long fromUserId;
    
    /**
     * 请求接收者ID
     */
    @TableField("to_user_id")
    private Long toUserId;
    
    /**
     * 请求状态: 0-待处理, 1-已同意, 2-已拒绝, 3-已过期
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 请求备注/留言
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 过期时间(默认为发送后7天)
     */
    @TableField("expires_time")
    private LocalDateTime expiresTime;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}