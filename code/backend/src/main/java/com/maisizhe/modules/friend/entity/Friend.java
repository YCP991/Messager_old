package com.maisizhe.modules.friend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
 * 好友关系实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("friend")
public class Friend {
    
    /**
     * 主键ID(自增)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 好友ID
     */
    @TableField("friend_id")
    private Long friendId;
    
    /**
     * 备注名
     */
    private String remark;
    
    /**
     * 是否置顶:0-否,1-是
     */
    @TableField("is_pinned")
    private Integer isPinned;
    
    /**
     * 是否免打扰:0-否,1-是
     */
    @TableField("is_muted")
    private Integer isMuted;
    
    /**
     * 排序权重(用于拖拽排序)
     */
    @TableField("sort_order")
    private Double sortOrder;
    
    /**
     * 添加时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除:0-未删除,1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}
