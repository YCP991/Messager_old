package com.maisizhe.modules.ai.controller;

import com.maisizhe.common.response.Result;
import com.maisizhe.modules.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI控制器
 * 提供AI对话和测试接口
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    
    private final AiService aiService;
    
    /**
     * AI对话测试接口
     * 
     * @param message 用户消息
     * @return AI回复
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestParam String message) {
        log.info("AI对话请求: {}", message);
        String response = aiService.chat(message);
        return Result.success(response);
    }
    
    /**
     * 意图分析测试接口
     * 
     * @param question 问题
     * @return 意图类型
     */
    @PostMapping("/intent")
    public Result<String> analyzeIntent(@RequestParam String question) {
        log.info("意图分析请求: {}", question);
        String intent = aiService.analyzeIntent(question);
        return Result.success(intent);
    }
}
