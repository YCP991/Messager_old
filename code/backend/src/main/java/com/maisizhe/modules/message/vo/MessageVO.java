package com.maisizhe.modules.message.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息响应VO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class MessageVO {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private String chatId;
    
    /**
     * 会话内序列号
     */
    private Long seqId;
    
    /**
     * 发送者ID
     */
    private Long fromUid;
    
    /**
     * 发送者姓名
     */
    private String fromName;
    
    /**
     * 发送者头像
     */
    private String fromAvatar;
    
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
     * 消息类型
     */
    private Integer msgType;
    
    /**
     * 消息状态:0-发送中,1-已发送,2-已送达,3-已读,4-发送失败
     */
    private Integer msgStatus;
    
    /**
     * @的用户ID列表
     */
    private List<Long> mentionedUsers;
    
    /**
     * 是否已撤回
     */
    private Integer isRecalled;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;
}
