package com.maisizhe.common.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一响应类单元测试
 */
class ResultTest {
    
    @Test
    void testSuccess() {
        // When
        Result<Void> result = Result.success();
        
        // Then
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    void testSuccessWithData() {
        // Given
        String testData = "test data";
        
        // When
        Result<String> result = Result.success(testData);
        
        // Then
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(testData, result.getData());
    }
    
    @Test
    void testError() {
        // When
        Result<Void> result = Result.error("error message");
        
        // Then
        assertEquals(500, result.getCode());
        assertEquals("error message", result.getMessage());
        assertNull(result.getData());
    }
    
    @Test
    void testErrorWithCode() {
        // When
        Result<Void> result = Result.error(404, "not found");
        
        // Then
        assertEquals(404, result.getCode());
        assertEquals("not found", result.getMessage());
    }
    
    @Test
    void testUnauthorized() {
        // When
        Result<Void> result = Result.unauthorized("unauthorized");
        
        // Then
        assertEquals(401, result.getCode());
        assertEquals("unauthorized", result.getMessage());
    }
    
    @Test
    void testForbidden() {
        // When
        Result<Void> result = Result.forbidden("forbidden");
        
        // Then
        assertEquals(403, result.getCode());
        assertEquals("forbidden", result.getMessage());
    }
    
    @Test
    void testNotFound() {
        // When
        Result<Void> result = Result.notFound("not found");
        
        // Then
        assertEquals(404, result.getCode());
        assertEquals("not found", result.getMessage());
    }
    
    @Test
    void testCustomResult() {
        // When
        Result<Integer> result = new Result<>(200, "custom message", 100);
        
        // Then
        assertEquals(200, result.getCode());
        assertEquals("custom message", result.getMessage());
        assertEquals(100, result.getData());
        assertNotNull(result.getTimestamp());
    }
    
    @Test
    void testTimestampIsSet() {
        // Given
        long beforeTime = System.currentTimeMillis();
        
        // When
        Result<Void> result = Result.success();
        
        // Then
        long afterTime = System.currentTimeMillis();
        assertTrue(result.getTimestamp() >= beforeTime);
        assertTrue(result.getTimestamp() <= afterTime);
    }
}
