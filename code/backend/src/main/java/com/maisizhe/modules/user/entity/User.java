package com.maisizhe.modules.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@TableName("user")
public class User {
    
    /**
     * 用户ID(雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户名(登录用)
     */
    private String username;
    
    /**
     * 密码(BCrypt加密)
     */
    private String password;
    
    /**
     * 学号/工号
     */
    private String studentNo;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 班级号(学生)
     */
    private String classNo;
    
    /**
     * 院系
     */
    private String department;
    
    /**
     * 角色:0-学生,1-教师,2-管理员
     */
    private Integer role;
    
    /**
     * 状态:0-禁用,1-正常
     */
    private Integer status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
