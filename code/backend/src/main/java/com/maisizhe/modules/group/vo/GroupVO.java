package com.maisizhe.modules.group.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群组信息VO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class GroupVO {
    
    /**
     * 群组ID
     */
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
     * 群组描述
     */
    private String description;
    
    /**
     * 群组类型
     */
    private Integer groupType;
    
    /**
     * 群组类型描述
     */
    private String groupTypeDesc;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 创建者姓名
     */
    private String creatorName;
    
    /**
     * 班级号
     */
    private String classNo;
    
    /**
     * 课程代码
     */
    private String courseCode;
    
    /**
     * 最大成员数
     */
    private Integer maxMembers;
    
    /**
     * 当前成员数
     */
    private Integer memberCount;
    
    /**
     * 是否已解散
     */
    private Integer isDisbanded;
    
    /**
     * 解散时间
     */
    private LocalDateTime disbandTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 当前用户在群中的角色(null表示未加入)
     */
    private Integer myRole;
}
