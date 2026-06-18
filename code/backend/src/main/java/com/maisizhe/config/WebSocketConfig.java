package com.maisizhe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * 
 * @author MaiSiZhe Team
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {
    
    /**
     * 注册ServerEndpointExporter
     * 支持使用@ServerEndpoint注解创建WebSocket端点
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
