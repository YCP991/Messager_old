package com.maisizhe.modules.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类
 * 
 * @author MaiSiZhe Team
 */
@Data
@TableName("im_message")
public class Message {
    
    /**
     * 消息ID(雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 会话ID(私聊:p_uid1_uid2, 群聊:g_gid)
     */
    private String chatId;
    
    /**
     * 会话内序列号(连续递增)
     */
    private Long seqId;
    
    /**
     * 发送者ID(0表示AI机器人)
     */
    private Long fromUid;
    
    /**
     * 接收者ID(私聊时有效)
     */
    private Long toUid;
    
    /**
     * 群组ID(群聊时有效)
     */
    private Long groupId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型:0-文本,1-图片,2-文件,3-AI摘要,99-AI思考中
     */
    private Integer msgType;
    
    /**
     * @的用户ID列表(JSON数组)
     */
    private String mentionedUsers;
    
    /**
     * 是否已撤回:0-否,1-是
     */
    private Integer isRecalled;
    
    /**
     * 撤回时间
     */
    private LocalDateTime recallTime;
    
    /**
     * 扩展数据(JSON格式)
     */
    private String extraData;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
