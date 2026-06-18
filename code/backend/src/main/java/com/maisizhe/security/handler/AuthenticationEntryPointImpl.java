package com.maisizhe.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maisizhe.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证入口点处理器
 * 处理未认证用户的访问请求
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        log.warn("用户未认证，拒绝访问: {}", request.getRequestURI());
        
        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        // 返回统一格式的响应
        Result<Void> result = Result.unauthorized("请先登录");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
