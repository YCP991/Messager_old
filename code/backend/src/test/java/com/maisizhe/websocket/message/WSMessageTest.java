package com.maisizhe.websocket.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket消息类单元测试
 */
class WSMessageTest {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testWSMessage_Creation() {
        // Given
        WSMessage message = new WSMessage();
        message.setType("GROUP_MESSAGE");
        message.setClientMsgId("client_msg_001");
        message.setTimestamp(System.currentTimeMillis());
        
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", 2001L);
        data.put("content", "测试消息");
        message.setData(data);
        
        // Then
        assertEquals("GROUP_MESSAGE", message.getType());
        assertEquals("client_msg_001", message.getClientMsgId());
        assertNotNull(message.getTimestamp());
        assertNotNull(message.getData());
    }
    
    @Test
    void testWSMessage_Serialization() throws Exception {
        // Given
        WSMessage message = new WSMessage();
        message.setType("PRIVATE_MESSAGE");
        message.setClientMsgId("client_msg_002");
        
        // When
        String json = objectMapper.writeValueAsString(message);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"PRIVATE_MESSAGE\""));
        assertTrue(json.contains("\"clientMsgId\":\"client_msg_002\""));
    }
    
    @Test
    void testWSMessage_Deserialization() throws Exception {
        // Given
        String json = "{\"type\":\"AI_RESPONSE\",\"clientMsgId\":\"ai_msg_001\",\"timestamp\":1705334400000}";
        
        // When
        WSMessage message = objectMapper.readValue(json, WSMessage.class);
        
        // Then
        assertEquals("AI_RESPONSE", message.getType());
        assertEquals("ai_msg_001", message.getClientMsgId());
        assertEquals(1705334400000L, message.getTimestamp());
    }
    
    @Test
    void testWSMessage_Types() {
        // Test different message types
        String[] types = {
            "GROUP_MESSAGE",
            "PRIVATE_MESSAGE",
            "AI_THINKING",
            "AI_RESPONSE",
            "MESSAGE_RECALL",
            "USER_ONLINE",
            "USER_OFFLINE"
        };
        
        for (String type : types) {
            WSMessage message = new WSMessage();
            message.setType(type);
            assertEquals(type, message.getType());
        }
    }
}
