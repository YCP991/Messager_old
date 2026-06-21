package com.maisizhe.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * 
 * 使用Jakarta WebSocket (@ServerEndpoint) 方式实现WebSocket
 * ServerEndpointExporter会自动扫描并注册所有@ServerEndpoint注解的类
 * 
 * @author MaiSiZhe Team
 */
@Configuration
@Profile("!test")
@ConditionalOnWebApplication
public class WebSocketConfig {
    
    /**
     * 注册ServerEndpointExporter
     * 支持使用@ServerEndpoint注解创建WebSocket端点
     * 
     * 注意：@ServerEndpoint注解的类（如WebSocketHandler）会自动被扫描注册，
     * 不需要通过WebSocketConfigurer手动注册
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
