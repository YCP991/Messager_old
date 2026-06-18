package com.maisizhe.modules.group.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群成员信息VO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class GroupMemberVO {
    
    /**
     * 成员ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
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
     * 成员角色:0-普通成员,1-管理员,2-群主
     */
    private Integer role;
    
    /**
     * 角色描述
     */
    private String roleDesc;
    
    /**
     * 入群时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 是否在线
     */
    private Boolean isOnline;
}
