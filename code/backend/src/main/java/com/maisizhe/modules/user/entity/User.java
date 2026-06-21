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
    @TableField("student_no")
    private String studentNo;
    
    /**
     * 真实姓名
     */
    @TableField("real_name")
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
    @TableField("class_no")
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
     * 在线状态:0-离线,1-在线
     */
    @TableField("is_online")
    private Integer isOnline;
    
    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;
    
    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
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
    
    /**
     * 逻辑删除:0-未删除,1-已删除
     */
    @TableField("deleted")
    private Integer deleted;
}
