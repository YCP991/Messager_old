package com.maisizhe.common.constants;

/**
 * Redis Key常量定义
 * 
 * @author MaiSiZhe Team
 */
public class RedisKeyConstants {
    
    /**
     * 用户在线状态前缀
     * 完整Key: online:user:{userId}
     */
    public static final String ONLINE_USER = "online:user:";
    
    /**
     * 消息序列号前缀
     * 完整Key: seq:chat:{chatId}
     */
    public static final String SEQ_ID_PREFIX = "seq:chat:";
    
    /**
     * 会话消息缓存前缀(ZSet)
     * 完整Key: chat:msg:{chatId}
     */
    public static final String CHAT_MSG_PREFIX = "chat:msg:";
    
    /**
     * 客户端消息ID去重前缀
     * 完整Key: client:msg:{clientMsgId}
     */
    public static final String CLIENT_MSG_ID = "client:msg:";
    
    /**
     * 私聊已读游标前缀
     * 完整Key: read:private:{userId}:{peerId}
     */
    public static final String PRIVATE_READ_CURSOR = "read:private:";
    
    /**
     * 群聊已读游标前缀
     * 完整Key: read:group:{userId}:{groupId}
     */
    public static final String GROUP_READ_CURSOR = "read:group:";
    
    /**
     * AI会话上下文前缀
     * 完整Key: ai:context:{sessionId}
     */
    public static final String AI_CONTEXT_PREFIX = "ai:context:";
    
    /**
     * Token黑名单前缀
     * 完整Key: token:blacklist:{token}
     */
    public static final String TOKEN_BLACKLIST = "token:blacklist:";
    
    /**
     * 文件上传锁前缀
     * 完整Key: file:lock:{fileName}
     */
    public static final String FILE_UPLOAD_LOCK = "file:lock:";
}
