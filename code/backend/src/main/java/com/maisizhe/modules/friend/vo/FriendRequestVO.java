package com.maisizhe.modules.friend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友请求VO
 * 
 * @author MaiSiZhe Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestVO {
    
    /**
     * 请求ID
     */
    private Long id;
    
    /**
     * 发起者ID
     */
    private Long fromUserId;
    
    /**
     * 发起者用户名
     */
    private String fromUsername;
    
    /**
     * 发起者真实姓名
     */
    private String fromRealName;
    
    /**
     * 发起者头像
     */
    private String fromAvatar;
    
    /**
     * 请求备注/留言
     */
    private String remark;
    
    /**
     * 请求状态: 0-待处理, 1-已同意, 2-已拒绝, 3-已过期
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}