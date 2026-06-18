package com.maisizhe.modules.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建群组请求DTO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class CreateGroupDTO {
    
    /**
     * 群组名称
     */
    @NotBlank(message = "群组名称不能为空")
    private String groupName;
    
    /**
     * 群组头像URL
     */
    private String groupAvatar;
    
    /**
     * 群组类型:0-普通群,1-班级群,2-课程群
     */
    @NotNull(message = "群组类型不能为空")
    private Integer groupType;
    
    /**
     * 班级号(班级群/课程群必填)
     */
    private String classNo;
    
    /**
     * 课程代码(课程群必填)
     */
    private String courseCode;
    
    /**
     * 最大成员数(0表示无限制)
     */
    private Integer maxMembers = 0;
    
    /**
     * 是否允许成员邀请:0-否,1-是
     */
    private Integer allowInvite = 1;
}
