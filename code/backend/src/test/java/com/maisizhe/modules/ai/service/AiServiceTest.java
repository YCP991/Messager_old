package com.maisizhe.modules.ai.service;

import com.maisizhe.modules.ai.service.impl.AiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI服务单元测试
 */
class AiServiceTest {
    
    private AiService aiService;
    
    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl();
    }
    
    @Test
    void testChat_ResponseNotNull() {
        // Given
        String message = "你好，请介绍一下自己";
        
        // When
        String response = aiService.chat(message);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
    
    @Test
    void testGenerateSummary_ValidInput() {
        // Given
        String messages = "[{\"content\":\"消息1\"},{\"content\":\"消息2\"}]";
        
        // When
        String summary = aiService.generateSummary(messages);
        
        // Then
        assertNotNull(summary);
        assertFalse(summary.isEmpty());
    }
    
    @Test
    void testAnalyzeIntent_SummaryIntent() {
        // Given
        String message = "帮我总结一下今天的讨论";
        
        // When
        String intent = aiService.analyzeIntent(message);
        
        // Then
        assertNotNull(intent);
        assertFalse(intent.isEmpty());
    }
    
    @Test
    void testAnalyzeIntent_QueryIntent() {
        // Given
        String message = "查询一下课程安排";
        
        // When
        String intent = aiService.analyzeIntent(message);
        
        // Then
        assertNotNull(intent);
        assertFalse(intent.isEmpty());
    }
    
    @Test
    void testAnalyzeIntent_ChatIntent() {
        // Given
        String message = "今天天气怎么样";
        
        // When
        String intent = aiService.analyzeIntent(message);
        
        // Then
        assertNotNull(intent);
        assertFalse(intent.isEmpty());
    }
    
    @Test
    void testChat_EmptyMessage() {
        // Given
        String message = "";
        
        // When
        String response = aiService.chat(message);
        
        // Then
        assertNotNull(response);
    }
    
    @Test
    void testGenerateSummary_ZeroMessages() {
        // Given
        String messages = "[]";
        
        // When
        String summary = aiService.generateSummary(messages);
        
        // Then
        assertNotNull(summary);
    }
}
