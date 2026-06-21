package com.maisizhe.common.config;

import com.maisizhe.common.interceptor.RequestLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器和配置跨域
 * 
 * @author MaiSiZhe Team
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final RequestLogInterceptor requestLogInterceptor;
    
    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/api/**") // 拦截所有API请求
                .excludePathPatterns(
                    "/api/auth/login",    // 登录接口
                    "/api/auth/register", // 注册接口
                    "/api/public/**"      // 公开接口
                );
    }
}