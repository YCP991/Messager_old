package com.maisizhe.modules.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private String groupName;
    
    /**
     * 群组头像URL
     */
    private String groupAvatar;
    
    /**
     * 群组公告
     */
    private String announcement;
    
    /**
     * 群组类型:0-普通群,1-班级群,2-课程群
     */
    private Integer groupType;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 班级号(班级群/课程群)
     */
    private String classNo;
    
    /**
     * 课程代码(课程群)
     */
    private String courseCode;
    
    /**
     * 最大成员数(0表示无限制)
     */
    private Integer maxMembers;
    
    /**
     * 是否允许成员邀请:0-否,1-是
     */
    private Integer allowInvite;
    
    /**
     * 当前成员数
     */
    private Integer memberCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
