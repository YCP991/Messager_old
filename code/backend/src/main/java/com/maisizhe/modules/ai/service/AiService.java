package com.maisizhe.modules.ai.service;

/**
 * AI服务接口
 * 提供AI对话和摘要生成能力
 * 
 * @author MaiSiZhe Team
 */
public interface AiService {
    
    /**
     * 与AI对话
     * 
     * @param message 用户消息
     * @return AI回复
     */
    String chat(String message);
    
    /**
     * 生成群聊摘要
     * 
     * @param messages 消息列表(JSON格式)
     * @return 摘要内容
     */
    String generateSummary(String messages);
    
    /**
     * 分析问题意图
     * 
     * @param question 问题
     * @return 意图类型(summary/query/chat)
     */
    String analyzeIntent(String question);
}
