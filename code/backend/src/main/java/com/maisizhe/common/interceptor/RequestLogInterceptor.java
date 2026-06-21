package com.maisizhe.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求日志拦截器
 * 记录所有HTTP请求的详细信息
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {
    
    private static final String REQUEST_ID = "requestId";
    private static final String START_TIME = "startTime";
    
    /**
     * 请求开始前记录
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute(REQUEST_ID, requestId);
        request.setAttribute(START_TIME, System.currentTimeMillis());
        
        // 记录请求信息
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("[{}] >>> {} {} {} | IP: {} | UA: {}", 
            requestId, method, uri, query != null ? "?" + query : "", ip, userAgent);
        
        // 将请求ID添加到响应头
        response.setHeader("X-Request-Id", requestId);
        
        return true;
    }
    
    /**
     * 请求完成后记录
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
            Object handler, Exception ex) {
        String requestId = (String) request.getAttribute(REQUEST_ID);
        Long startTime = (Long) request.getAttribute(START_TIME);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        int status = response.getStatus();
        
        if (ex != null) {
            log.error("[{}] <<< {} | {}ms | Exception: {}", 
                requestId, status, duration, ex.getMessage());
        } else if (status >= 400) {
            log.warn("[{}] <<< {} | {}ms", requestId, status, duration);
        } else {
            log.info("[{}] <<< {} | {}ms", requestId, status, duration);
        }
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}