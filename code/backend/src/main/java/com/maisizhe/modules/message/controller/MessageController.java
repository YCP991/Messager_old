package com.maisizhe.modules.message.controller;

import com.maisizhe.common.response.Result;
import com.maisizhe.modules.message.dto.SendMessageDTO;
import com.maisizhe.modules.message.service.MessageService;
import com.maisizhe.modules.message.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 * 处理消息发送、查询等接口
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    
    /**
     * 发送私聊消息
     * 
     * @param fromUid 发送者ID(从JWT中获取)
     * @param dto 消息请求
     * @return 消息响应
     */
    @PostMapping("/private")
    public Result<MessageVO> sendPrivateMessage(
            @RequestParam Long fromUid,
            @Valid @RequestBody SendMessageDTO dto) {
        log.info("发送私聊消息: fromUid={}, toUid={}", fromUid, dto.getToUid());
        MessageVO messageVO = messageService.sendPrivateMessage(fromUid, dto);
        return Result.success(messageVO);
    }
    
    /**
     * 发送群聊消息
     * 
     * @param fromUid 发送者ID(从JWT中获取)
     * @param dto 消息请求
     * @return 消息响应
     */
    @PostMapping("/group")
    public Result<MessageVO> sendGroupMessage(
            @RequestParam Long fromUid,
            @Valid @RequestBody SendMessageDTO dto) {
        log.info("发送群聊消息: fromUid={}, groupId={}", fromUid, dto.getGroupId());
        MessageVO messageVO = messageService.sendGroupMessage(fromUid, dto);
        return Result.success(messageVO);
    }
    
    /**
     * 获取私聊历史消息
     * 
     * @param userId 当前用户ID(从JWT中获取)
     * @param peerId 对方用户ID
     * @param limit 数量限制(默认50)
     * @return 消息列表
     */
    @GetMapping("/private/history")
    public Result<List<MessageVO>> getPrivateHistory(
            @RequestParam Long userId,
            @RequestParam Long peerId,
            @RequestParam(defaultValue = "50") Integer limit) {
        List<MessageVO> messages = messageService.getPrivateHistory(userId, peerId, limit);
        return Result.success(messages);
    }
    
    /**
     * 获取群聊历史消息
     * 
     * @param groupId 群组ID
     * @param limit 数量限制(默认50)
     * @return 消息列表
     */
    @GetMapping("/group/history")
    public Result<List<MessageVO>> getGroupHistory(
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "50") Integer limit) {
        List<MessageVO> messages = messageService.getGroupHistory(groupId, limit);
        return Result.success(messages);
    }
    
    /**
     * 撤回消息
     * 
     * @param messageId 消息ID
     * @param userId 操作用户ID(从JWT中获取)
     * @return 成功响应
     */
    @PostMapping("/recall")
    public Result<Void> recallMessage(
            @RequestParam Long messageId,
            @RequestParam Long userId) {
        log.info("撤回消息: messageId={}, userId={}", messageId, userId);
        messageService.recallMessage(messageId, userId);
        return Result.success();
    }
}
