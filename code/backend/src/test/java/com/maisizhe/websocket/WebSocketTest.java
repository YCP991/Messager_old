package com.maisizhe.websocket;

import com.maisizhe.websocket.session.UserSessionManager;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * WebSocket会话管理单元测试
 * 
 * @author MaiSiZhe Team
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebSocketTest {

    @Mock
    private Session session1;

    @Mock
    private Session session2;

    private UserSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        // 初始化会话管理器
        sessionManager = new UserSessionManager();
        
        // Mock Session行为
        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
    }

    @Test
    @DisplayName("添加会话成功测试")
    void testAddSessionSuccess() {
        // 执行测试
        sessionManager.addSession(1L, session1);

        // 验证结果
        assertTrue(sessionManager.isOnline(1L));
        assertEquals(session1, sessionManager.getSession(1L));
    }

    @Test
    @DisplayName("添加多个会话测试")
    void testAddMultipleSessions() {
        // 执行测试
        sessionManager.addSession(1L, session1);
        sessionManager.addSession(2L, session2);

        // 验证结果
        assertTrue(sessionManager.isOnline(1L));
        assertTrue(sessionManager.isOnline(2L));
        assertEquals(session1, sessionManager.getSession(1L));
        assertEquals(session2, sessionManager.getSession(2L));
    }

    @Test
    @DisplayName("移除会话成功测试")
    void testRemoveSessionSuccess() {
        // 先添加会话
        sessionManager.addSession(1L, session1);
        assertTrue(sessionManager.isOnline(1L));

        // 移除会话
        sessionManager.removeSession(1L);

        // 验证结果
        assertFalse(sessionManager.isOnline(1L));
        assertNull(sessionManager.getSession(1L));
    }

    @Test
    @DisplayName("移除不存在会话测试")
    void testRemoveNonExistentSession() {
        // 移除不存在的会话(不应抛出异常)
        sessionManager.removeSession(999L);

        // 验证结果
        assertFalse(sessionManager.isOnline(999L));
    }

    @Test
    @DisplayName("检查用户在线状态测试")
    void testIsOnline() {
        // 添加会话
        sessionManager.addSession(1L, session1);

        // 验证在线状态
        assertTrue(sessionManager.isOnline(1L));
        assertFalse(sessionManager.isOnline(2L));
    }

    @Test
    @DisplayName("获取所有在线用户测试")
    void testGetAllOnlineUsers() {
        // 添加多个会话
        sessionManager.addSession(1L, session1);
        sessionManager.addSession(2L, session2);

        // 获取在线用户列表
        Map<Long, Session> onlineUsers = sessionManager.getAllSessions();

        // 验证结果
        assertNotNull(onlineUsers);
        assertEquals(2, onlineUsers.size());
        assertTrue(onlineUsers.containsKey(1L));
        assertTrue(onlineUsers.containsKey(2L));
    }

    @Test
    @DisplayName("获取在线用户数测试")
    void testGetOnlineCount() {
        // 添加会话
        sessionManager.addSession(1L, session1);
        sessionManager.addSession(2L, session2);

        // 获取在线用户数
        int count = sessionManager.getOnlineCount();

        // 验证结果
        assertEquals(2, count);
    }

    @Test
    @DisplayName("替换已存在会话测试")
    void testReplaceExistingSession() {
        // 先添加session1
        sessionManager.addSession(1L, session1);
        assertEquals(session1, sessionManager.getSession(1L));

        // 替换为session2
        sessionManager.addSession(1L, session2);
        assertEquals(session2, sessionManager.getSession(1L));
    }

    @Test
    @DisplayName("并发添加会话测试")
    void testConcurrentAddSession() throws InterruptedException {
        // 创建线程模拟并发添加
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sessionManager.addSession(1L + i, mock(Session.class));
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sessionManager.addSession(101L + i, mock(Session.class));
            }
        });

        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程完成
        thread1.join();
        thread2.join();

        // 验证结果
        assertEquals(200, sessionManager.getOnlineCount());
    }

    @Test
    @DisplayName("并发移除会话测试")
    void testConcurrentRemoveSession() throws InterruptedException {
        // 先添加200个会话
        for (int i = 0; i < 200; i++) {
            sessionManager.addSession(1L + i, mock(Session.class));
        }

        // 创建线程模拟并发移除
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sessionManager.removeSession(1L + i);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                sessionManager.removeSession(101L + i);
            }
        });

        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程完成
        thread1.join();
        thread2.join();

        // 验证结果
        assertEquals(0, sessionManager.getOnlineCount());
    }
}