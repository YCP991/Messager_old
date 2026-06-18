package com.maisizhe.websocket.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * 用户会话管理器
 * 管理在线用户的WebSocket Session
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Component
public class UserSessionManager {
    
    /**
     * 存储用户ID和WebSocket Session的映射关系
     * Key: userId
     * Value: WebSocket Session
     */
    private final Map<Long, Session> userSessions = new ConcurrentHashMap<>();
    
    /**
     * 添加用户会话
     * 
     * @param userId 用户ID
     * @param session WebSocket会话
     */
    public void addSession(Long userId, Session session) {
        if (userId == null || session == null) {
            return;
        }
        userSessions.put(userId, session);
        log.info("用户{}上线，当前在线人数: {}", userId, userSessions.size());
    }
    
    /**
     * 移除用户会话
     * 
     * @param userId 用户ID
     */
    public void removeSession(Long userId) {
        if (userId == null) {
            return;
        }
        userSessions.remove(userId);
        log.info("用户{}下线，当前在线人数: {}", userId, userSessions.size());
    }
    
    /**
     * 获取用户会话
     * 
     * @param userId 用户ID
     * @return WebSocket会话
     */
    public Session getSession(Long userId) {
        return userSessions.get(userId);
    }
    
    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return true-在线，false-离线
     */
    public boolean isOnline(Long userId) {
        return userSessions.containsKey(userId);
    }
    
    /**
     * 获取在线用户数量
     * 
     * @return 在线用户数
     */
    public int getOnlineCount() {
        return userSessions.size();
    }
    
    /**
     * 获取所有在线用户ID
     * 
     * @return 在线用户ID集合
     */
    public Map<Long, Session> getAllSessions() {
        return new ConcurrentHashMap<>(userSessions);
    }
}
