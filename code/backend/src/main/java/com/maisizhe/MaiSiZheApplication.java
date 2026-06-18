package com.maisizhe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 麦思哲(MaiSiZhe)应用启动类
 * 
 * @author MaiSiZhe Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync  // 启用异步支持
@EnableScheduling  // 启用定时任务
public class MaiSiZheApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MaiSiZheApplication.class, args);
        System.out.println("========================================");
        System.out.println("   麦思哲(MaiSiZhe)后端服务启动成功!");
        System.out.println("========================================");
    }
}
