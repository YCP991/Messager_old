package com.maisizhe.modules.ai.service;

import com.maisizhe.modules.message.dto.SendMessageDTO;

/**
 * AI工作流服务接口
 * 处理@AI触发的异步工作流
 * 
 * @author MaiSiZhe Team
 */
public interface AiWorkflowService {
    
    /**
     * 处理@AI消息
     * 立即返回"AI思考中"占位消息，然后异步处理
     * 
     * @param groupId 群组ID
     * @param fromUid 发送者ID
     * @param content 消息内容
     * @param clientMsgId 客户端消息ID
     */
    void handleAtAiMessage(Long groupId, Long fromUid, String content, String clientMsgId);
    
    /**
     * 异步执行AI任务
     * 
     * @param groupId 群组ID
     * @param fromUid 发送者ID
     * @param content 消息内容
     */
    void executeAiTask(Long groupId, Long fromUid, String content);
}
