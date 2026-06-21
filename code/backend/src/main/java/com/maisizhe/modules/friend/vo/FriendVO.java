package com.maisizhe.modules.friend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 好友VO - 返回给前端的友好格式
 * 
 * @author MaiSiZhe Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendVO {
    
    /**
     * 好友ID
     */
    private Long friendId;
    
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
     * 班级号
     */
    private String classNo;
    
    /**
     * 院系
     */
    private String department;
    
    /**
     * 备注名
     */
    private String remark;
    
    /**
     * 是否置顶:0-否,1-是
     */
    private Integer isPinned;
    
    /**
     * 是否免打扰:0-否,1-是
     */
    private Integer isMuted;
    
    /**
     * 排序权重
     */
    private Double sortOrder;
    
    /**
     * 是否在线:0-否,1-是
     */
    private Integer isOnline;
    
    /**
     * 添加时间
     */
    private String createTime;
}
