package com.maisizhe.modules.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群成员实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@TableName("group_member")
public class GroupMember {
    
    /**
     * 成员ID(雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 成员角色:0-普通成员,1-管理员,2-群主
     */
    private Integer role;
    
    /**
     * 入群时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 最后阅读消息seq_id
     */
    private Long lastReadSeqId;
    
    /**
     * 是否已退群:0-否,1-是
     */
    private Integer isQuit;
    
    /**
     * 退群时间
     */
    private LocalDateTime quitTime;
}
