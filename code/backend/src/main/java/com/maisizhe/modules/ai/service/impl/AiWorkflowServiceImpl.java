package com.maisizhe.modules.ai.service.impl;

import com.maisizhe.common.enums.MessageTypeEnum;
import com.maisizhe.modules.ai.service.AiService;
import com.maisizhe.modules.ai.service.AiWorkflowService;
import com.maisizhe.modules.message.dto.SendMessageDTO;
import com.maisizhe.modules.message.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

/**
 * AI工作流服务实现类
 * 异步处理@AI消息，避免阻塞主线程
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Service
public class AiWorkflowServiceImpl implements AiWorkflowService {
    
    private final MessageService messageService;
    private final AiService aiService;
    private final Executor aiTaskExecutor;
    
    public AiWorkflowServiceImpl(@Lazy MessageService messageService, 
                                  AiService aiService,
                                  Executor aiTaskExecutor) {
        this.messageService = messageService;
        this.aiService = aiService;
        this.aiTaskExecutor = aiTaskExecutor;
    }
    
    /**
     * 处理@AI消息
     * 立即返回"AI思考中"占位消息，然后异步处理
     */
    @Override
    public void handleAtAiMessage(Long groupId, Long fromUid, String content, String clientMsgId) {
        log.info("收到@AI消息: groupId={}, fromUid={}, content={}", groupId, fromUid, content);
        
        // 1. 立即发送"AI思考中"占位消息
        sendThinkingMessage(groupId, clientMsgId);
        
        // 2. 异步执行AI任务
        aiTaskExecutor.execute(() -> {
            try {
                executeAiTask(groupId, fromUid, content);
            } catch (Exception e) {
                log.error("AI任务执行失败", e);
                sendErrorMessage(groupId, fromUid);
            }
        });
        
        log.info("@AI消息已入队处理");
    }
    
    /**
     * 异步执行AI任务
     */
    @Override
    public void executeAiTask(Long groupId, Long fromUid, String content) {
        log.info("开始执行AI任务: groupId={}, content={}", groupId, content);
        
        // 1. 分析用户意图
        String intent = aiService.analyzeIntent(content);
        log.info("AI意图分析结果: {}", intent);
        
        String response;
        switch (intent) {
            case "summary":
                // 生成群聊摘要
                response = generateGroupSummary(groupId);
                break;
            case "query":
                // 查询类问题(暂时简化为对话)
                response = aiService.chat(content);
                break;
            default:
                // 普通对话
                response = aiService.chat(content);
                break;
        }
        
        // 2. 发送AI回复
        sendAiResponse(groupId, fromUid, response);
        
        log.info("AI任务执行完成: groupId={}", groupId);
    }
    
    /**
     * 发送"AI思考中"占位消息
     */
    private void sendThinkingMessage(Long groupId, String clientMsgId) {
        try {
            SendMessageDTO dto = new SendMessageDTO();
            dto.setGroupId(groupId);
            dto.setContent("🤔 AI正在思考中...");
            dto.setMsgType(MessageTypeEnum.AI_PLANNING.getCode());
            dto.setClientMsgId(clientMsgId + "_thinking");
            
            // AI机器人ID为0
            messageService.sendGroupMessage(0L, dto);
            
            log.debug("已发送AI思考中消息: groupId={}", groupId);
        } catch (Exception e) {
            log.error("发送AI思考中消息失败", e);
        }
    }
    
    /**
     * 发送AI回复
     */
    private void sendAiResponse(Long groupId, Long fromUid, String response) {
        try {
            SendMessageDTO dto = new SendMessageDTO();
            dto.setGroupId(groupId);
            dto.setContent(response);
            dto.setMsgType(MessageTypeEnum.TEXT.getCode());
            dto.setClientMsgId("ai_response_" + System.currentTimeMillis());
            
            // AI机器人ID为0
            messageService.sendGroupMessage(0L, dto);
            
            log.debug("已发送AI回复: groupId={}", groupId);
        } catch (Exception e) {
            log.error("发送AI回复失败", e);
        }
    }
    
    /**
     * 发送错误消息
     */
    private void sendErrorMessage(Long groupId, Long fromUid) {
        try {
            SendMessageDTO dto = new SendMessageDTO();
            dto.setGroupId(groupId);
            dto.setContent("❌ 抱歉，AI服务暂时不可用，请稍后重试。");
            dto.setMsgType(MessageTypeEnum.TEXT.getCode());
            dto.setClientMsgId("ai_error_" + System.currentTimeMillis());
            
            messageService.sendGroupMessage(0L, dto);
            
            log.debug("已发送AI错误消息: groupId={}", groupId);
        } catch (Exception e) {
            log.error("发送AI错误消息失败", e);
        }
    }
    
    /**
     * 生成群聊摘要
     */
    private String generateGroupSummary(Long groupId) {
        // TODO: 从消息服务获取最近的消息
        // 这里使用模拟数据
        
        String mockMessages = "[{\"from\": \"张三\", \"content\": \"大家好\"}, {\"from\": \"李四\", \"content\": \"你好\"}]";
        
        return aiService.generateSummary(mockMessages);
    }
}
