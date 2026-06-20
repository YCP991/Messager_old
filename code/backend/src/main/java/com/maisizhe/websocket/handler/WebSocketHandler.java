package com.maisizhe.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maisizhe.modules.ai.service.AiWorkflowService;
import com.maisizhe.security.jwt.JwtUtil;
import com.maisizhe.websocket.message.WSMessage;
import com.maisizhe.websocket.session.UserSessionManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket处理器
 * 处理WebSocket连接、消息、关闭等事件
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{token}")
public class WebSocketHandler {
    
    // 注意: WebSocket端点不能使用@RequiredArgsConstructor，必须有无参构造函数
    // 使用@Autowired注入依赖
    @Autowired
    private UserSessionManager sessionManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AiWorkflowService aiWorkflowService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 存储每个Session对应的用户ID
     */
    private static final ConcurrentHashMap<Session, Long> SESSION_USER_MAP = new ConcurrentHashMap<>();
    
    /**
     * 连接建立成功调用
     * 
     * @param session WebSocket会话
     * @param token JWT Token
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证JWT Token
            if (!jwtUtil.validateToken(token)) {
                log.warn("WebSocket连接失败: Token无效");
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Token无效"));
                return;
            }
            
            // 获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("WebSocket连接失败: 无法获取用户ID");
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "用户ID无效"));
                return;
            }
            
            // 保存用户会话
            sessionManager.addSession(userId, session);
            SESSION_USER_MAP.put(session, userId);
            
            // 发送欢迎消息
            sendTextMessage(session, objectMapper.writeValueAsString(
                new WSMessage("SYSTEM_NOTIFICATION", "连接成功", null)
            ));
            
            log.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());
            
        } catch (Exception e) {
            log.error("WebSocket连接建立失败", e);
            try {
                session.close();
            } catch (IOException ex) {
                log.error("关闭WebSocket连接失败", ex);
            }
        }
    }
    
    /**
     * 收到客户端消息
     * 
     * @param message 消息内容
     * @param session WebSocket会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Long userId = SESSION_USER_MAP.get(session);
            if (userId == null) {
                log.warn("收到消息但用户未登录: sessionId={}", session.getId());
                return;
            }
            
            log.info("收到用户{}的消息: {}", userId, message);
            
            // 解析消息
            WSMessage wsMessage = objectMapper.readValue(message, WSMessage.class);
            
            // 根据消息类型处理
            handleMessage(userId, wsMessage, session);
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }
    
    /**
     * 连接关闭调用
     * 
     * @param session WebSocket会话
     */
    @OnClose
    public void onClose(Session session) {
        Long userId = SESSION_USER_MAP.remove(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
            log.info("WebSocket连接关闭: userId={}", userId);
        }
    }
    
    /**
     * 发生错误时调用
     * 
     * @param session WebSocket会话
     * @param error 错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        Long userId = SESSION_USER_MAP.get(session);
        log.error("WebSocket错误: userId={}", userId, error);
    }
    
    /**
     * 处理不同类型的消息
     * 
     * @param userId 用户ID
     * @param message 消息对象
     * @param session WebSocket会话
     */
    private void handleMessage(Long userId, WSMessage message, Session session) {
        switch (message.getType()) {
            case "HEARTBEAT":
                // 心跳响应
                handleHeartbeat(userId, session);
                break;
            case "PRIVATE_MESSAGE":
                // 私聊消息(后续实现)
                log.info("收到私聊消息: fromUserId={}", userId);
                break;
            case "GROUP_MESSAGE":
                // 群聊消息
                handleGroupMessage(userId, message);
                break;
            default:
                log.warn("未知消息类型: {}", message.getType());
        }
    }
    
    /**
     * 处理群聊消息
     * 
     * @param userId 用户ID
     * @param message 消息对象
     */
    @SuppressWarnings("unchecked")
    private void handleGroupMessage(Long userId, WSMessage message) {
        try {
            Map<String, Object> data = (Map<String, Object>) message.getData();
            Long groupId = ((Number) data.get("groupId")).longValue();
            String content = (String) data.get("content");
            String clientMsgId = message.getClientMsgId();
            
            // 检测是否@AI
            if (content != null && content.contains("@AI")) {
                log.info("检测到@AI消息: userId={}, groupId={}", userId, groupId);
                aiWorkflowService.handleAtAiMessage(groupId, userId, content, clientMsgId);
            } else {
                log.info("普通群聊消息: userId={}, groupId={}", userId, groupId);
                // TODO: 调用消息服务保存和推送
            }
        } catch (Exception e) {
            log.error("处理群聊消息失败", e);
        }
    }
    
    /**
     * 处理心跳
     * 
     * @param userId 用户ID
     * @param session WebSocket会话
     */
    private void handleHeartbeat(Long userId, Session session) {
        try {
            sendTextMessage(session, objectMapper.writeValueAsString(
                new WSMessage("HEARTBEAT", "pong", null)
            ));
            log.debug("心跳响应: userId={}", userId);
        } catch (Exception e) {
            log.error("发送心跳响应失败", e);
        }
    }
    
    /**
     * 发送文本消息
     * 
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void sendTextMessage(Session session, String message) {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("发送WebSocket消息失败", e);
                }
            }
        }
    }
    
    /**
     * 发送消息给指定用户
     * 
     * @param userId 用户ID
     * @param message 消息对象
     */
    public void sendMessageToUser(Long userId, WSMessage message) {
        Session session = sessionManager.getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                sendTextMessage(session, jsonMessage);
                log.debug("消息推送成功: userId={}", userId);
            } catch (Exception e) {
                log.error("消息推送失败: userId={}", userId, e);
            }
        } else {
            log.debug("用户不在线: userId={}", userId);
        }
    }
    
    /**
     * 广播消息给所有在线用户
     * 
     * @param message 消息对象
     */
    public void broadcastMessage(WSMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            sessionManager.getAllSessions().forEach((userId, session) -> {
                sendTextMessage(session, jsonMessage);
            });
            log.debug("广播消息成功: 在线人数={}", sessionManager.getOnlineCount());
        } catch (Exception e) {
            log.error("广播消息失败", e);
        }
    }
}
