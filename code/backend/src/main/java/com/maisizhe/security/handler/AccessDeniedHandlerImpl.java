package com.maisizhe.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maisizhe.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 访问拒绝处理器
 * 处理已认证但无权限的用户访问请求
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        
        log.warn("用户无权限访问: {}, 原因: {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        // 设置响应状态码和内容类型
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        // 返回统一格式的响应
        Result<Void> result = Result.forbidden("无权访问该资源");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
