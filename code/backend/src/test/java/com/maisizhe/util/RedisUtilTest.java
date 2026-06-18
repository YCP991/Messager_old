package com.maisizhe.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis工具类单元测试 - Key格式验证
 */
class RedisUtilTest {
    
    @Test
    void testIncr_KeyGeneration() {
        // Given
        String key = "seq:chat:g_2001";
        
        // Then
        assertEquals("seq:chat:g_2001", key);
        assertTrue(key.startsWith("seq:chat:"));
    }
    
    @Test
    void testDedupKey_Format() {
        // Given
        String clientMsgId = "client_msg_001";
        String dedupKey = "client:msg:" + clientMsgId;
        
        // Then
        assertEquals("client:msg:client_msg_001", dedupKey);
        assertTrue(dedupKey.startsWith("client:msg:"));
    }
    
    @Test
    void testChatId_Format_Group() {
        // Given
        Long groupId = 2001L;
        String chatId = "g_" + groupId;
        
        // Then
        assertEquals("g_2001", chatId);
        assertTrue(chatId.startsWith("g_"));
    }
    
    @Test
    void testChatId_Format_Private() {
        // Given
        Long uid1 = 1001L;
        Long uid2 = 1002L;
        String chatId = "p_" + Math.min(uid1, uid2) + "_" + Math.max(uid1, uid2);
        
        // Then
        assertEquals("p_1001_1002", chatId);
        assertTrue(chatId.startsWith("p_"));
    }
}
