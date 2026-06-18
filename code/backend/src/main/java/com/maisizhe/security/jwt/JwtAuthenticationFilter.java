package com.maisizhe.security.jwt;

import com.maisizhe.common.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 * 拦截请求，验证JWT Token，设置用户认证信息
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    /**
     * Token在请求头中的前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 请求头中Token的键名
     */
    private static final String HEADER_AUTHORIZATION = "Authorization";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求头获取Token
            String token = extractTokenFromRequest(request);
            
            // 验证Token
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 从Token中获取用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                Integer role = jwtUtil.getRoleFromToken(token);
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                new ArrayList<>()
                        );
                
                // 设置到Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("用户认证成功: userId={}, username={}", userId, username);
            }
        } catch (BusinessException e) {
            log.error("JWT认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        
        // 继续过滤链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求头中提取Token
     * 
     * @param request HTTP请求
     * @return JWT Token字符串
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        
        return null;
    }
}
