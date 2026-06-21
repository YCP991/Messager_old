package com.maisizhe.modules.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群组实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@TableName("im_group")
public class Group {
    
    /**
     * 群组ID(雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 群组名称
     */
    @TableField("name")
    private String groupName;
    
    /**
     * 群组头像URL
     */
    @TableField("avatar")
    private String groupAvatar;
    
    /**
     * 群组公告
     */
    private String announcement;
    
    /**
     * 群组描述
     */
    private String description;
    
    /**
     * 群组类型:0-普通群,1-班级群,2-课程群
     */
    @TableField("type")
    private Integer groupType;
    
    /**
     * 创建者ID
     */
    @TableField("owner_id")
    private Long creatorId;
    
    /**
     * 班级号(班级群/课程群)
     */
    @TableField("class_no")
    private String classNo;
    
    /**
     * 课程代码(课程群)
     */
    @TableField("course_id")
    private Long courseId;
    
    /**
     * 最大成员数(0表示无限制)
     */
    @TableField("max_members")
    private Integer maxMembers;
    
    /**
     * 当前成员数
     */
    @TableField("member_count")
    private Integer memberCount;
    
    /**
     * 是否已解散:0-否,1-是
     */
    @TableField("is_disbanded")
    private Integer isDisbanded;
    
    /**
     * 解散时间
     */
    @TableField("disband_time")
    private LocalDateTime disbandTime;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除:0-未删除,1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}
