package com.maisizhe.modules.user.vo;

import lombok.Data;

/**
 * 登录响应VO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class LoginVO {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
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
         * 角色
         */
        private Integer role;
        
        /**
         * 班级号
         */
        private String className;
    }
}
