package com.maisizhe.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类单元测试
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
            "TestSecretKeyForJWTTokenGenerationAndValidation2024",
            86400000L // 24小时
        );
    }
    
    @Test
    void testGenerateToken() {
        // Given
        Long userId = 1001L;
        String username = "testuser";
        Integer role = 0;
        
        // When
        String token = jwtUtil.generateToken(userId, username, role);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testValidateToken_ValidToken() {
        // Given
        Long userId = 1001L;
        String username = "testuser";
        Integer role = 0;
        String token = jwtUtil.generateToken(userId, username, role);
        
        // When
        boolean isValid = jwtUtil.validateToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void testValidateToken_EmptyToken() {
        // Given
        String emptyToken = "";
        
        // When
        boolean isValid = jwtUtil.validateToken(emptyToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void testGetUserIdFromToken() {
        // Given
        Long userId = 1001L;
        String username = "testuser";
        Integer role = 0;
        String token = jwtUtil.generateToken(userId, username, role);
        
        // When
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);
        
        // Then
        assertEquals(userId, extractedUserId);
    }
    
    @Test
    void testGetUserIdFromToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        Long userId = jwtUtil.getUserIdFromToken(invalidToken);
        
        // Then
        assertNull(userId);
    }
    
    @Test
    void testTokenExpiration() throws InterruptedException {
        // Given
        JwtUtil shortLivedJwtUtil = new JwtUtil(
            "TestSecretKeyForJWTTokenGenerationAndValidation2024",
            100L // 100毫秒过期
        );
        Long userId = 1001L;
        String username = "testuser";
        Integer role = 0;
        String token = shortLivedJwtUtil.generateToken(userId, username, role);
        
        // When - 等待token过期
        Thread.sleep(200);
        
        // Then
        assertFalse(shortLivedJwtUtil.validateToken(token));
    }
    
    @Test
    void testDifferentUsersGenerateDifferentTokens() {
        // Given
        Long userId1 = 1001L;
        Long userId2 = 1002L;
        String username1 = "user1";
        String username2 = "user2";
        Integer role = 0;
        
        // When
        String token1 = jwtUtil.generateToken(userId1, username1, role);
        String token2 = jwtUtil.generateToken(userId2, username2, role);
        
        // Then
        assertNotEquals(token1, token2);
    }
}
