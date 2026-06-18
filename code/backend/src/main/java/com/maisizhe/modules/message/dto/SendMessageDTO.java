package com.maisizhe.modules.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发送消息请求DTO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class SendMessageDTO {
    
    /**
     * 接收者ID(私聊时必填)
     */
    private Long toUid;
    
    /**
     * 群组ID(群聊时必填)
     */
    private Long groupId;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息类型(默认0-文本)
     */
    private Integer msgType = 0;
    
    /**
     * @的用户ID列表
     */
    private List<Long> mentionedUsers;
    
    /**
     * 客户端消息ID(用于去重)
     */
    @NotBlank(message = "客户端消息ID不能为空")
    private String clientMsgId;
}
