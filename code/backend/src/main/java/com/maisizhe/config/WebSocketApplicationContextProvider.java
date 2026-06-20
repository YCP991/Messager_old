package com.maisizhe.config;

import com.maisizhe.websocket.handler.WebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * WebSocket ApplicationContext提供者
 * 
 * 由于@ServerEndpoint注解的类不能直接使用@Autowired注入，
 * 本类确保ApplicationContext在应用启动时就被设置到WebSocketHandler中
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketApplicationContextProvider {
    
    private final ApplicationContext applicationContext;
    
    /**
     * 在Spring容器初始化完成后，将ApplicationContext设置到WebSocketHandler
     */
    @PostConstruct
    public void init() {
        WebSocketHandler.setApplicationContext(applicationContext);
        log.info("WebSocketHandler ApplicationContext已通过Provider设置");
    }
}
