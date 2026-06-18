package com.maisizhe.websocket.message;

import lombok.Data;

/**
 * WebSocket消息统一格式
 * 
 * @author MaiSiZhe Team
 */
@Data
public class WSMessage {
    
    /**
     * 消息类型
     * - PRIVATE_MESSAGE: 私聊消息
     * - GROUP_MESSAGE: 群聊消息
     * - SYSTEM_NOTIFICATION: 系统通知
     * - HEARTBEAT: 心跳
     */
    private String type;
    
    /**
     * 消息数据(JSON格式)
     */
    private Object data;
    
    /**
     * 客户端消息ID(用于去重)
     */
    private String clientMsgId;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public WSMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public WSMessage(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public WSMessage(String type, Object data, String clientMsgId) {
        this.type = type;
        this.data = data;
        this.clientMsgId = clientMsgId;
        this.timestamp = System.currentTimeMillis();
    }
}
