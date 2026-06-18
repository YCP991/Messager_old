package com.maisizhe.modules.user.vo;

import lombok.Data;

/**
 * 用户信息VO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class UserVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
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
     * 班级号
     */
    private String classNo;
    
    /**
     * 院系
     */
    private String department;
    
    /**
     * 角色
     */
    private Integer role;
}
