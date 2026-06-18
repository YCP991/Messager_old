package com.maisizhe.modules.message.service;

import com.maisizhe.modules.message.dto.SendMessageDTO;
import com.maisizhe.modules.message.vo.MessageVO;

import java.util.List;

/**
 * 消息服务接口
 * 
 * @author MaiSiZhe Team
 */
public interface MessageService {
    
    /**
     * 发送私聊消息
     * 
     * @param fromUid 发送者ID
     * @param dto 消息请求
     * @return 消息响应
     */
    MessageVO sendPrivateMessage(Long fromUid, SendMessageDTO dto);
    
    /**
     * 发送群聊消息
     * 
     * @param fromUid 发送者ID
     * @param dto 消息请求
     * @return 消息响应
     */
    MessageVO sendGroupMessage(Long fromUid, SendMessageDTO dto);
    
    /**
     * 获取私聊历史消息
     * 
     * @param userId 当前用户ID
     * @param peerId 对方用户ID
     * @param limit 数量限制
     * @return 消息列表
     */
    List<MessageVO> getPrivateHistory(Long userId, Long peerId, Integer limit);
    
    /**
     * 获取群聊历史消息
     * 
     * @param groupId 群组ID
     * @param limit 数量限制
     * @return 消息列表
     */
    List<MessageVO> getGroupHistory(Long groupId, Integer limit);
    
    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param userId 操作用户ID
     */
    void recallMessage(Long messageId, Long userId);
}
