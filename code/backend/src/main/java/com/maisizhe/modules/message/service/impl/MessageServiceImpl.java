package com.maisizhe.modules.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maisizhe.common.constants.RedisKeyConstants;
import com.maisizhe.common.enums.MemberRoleEnum;
import com.maisizhe.common.enums.MessageTypeEnum;
import com.maisizhe.common.exception.BusinessException;
import com.maisizhe.common.util.RedisUtil;
import com.maisizhe.modules.group.entity.Group;
import com.maisizhe.modules.group.entity.GroupMember;
import com.maisizhe.modules.group.mapper.GroupMapper;
import com.maisizhe.modules.group.mapper.GroupMemberMapper;
import com.maisizhe.modules.message.dto.SendMessageDTO;
import com.maisizhe.modules.message.entity.Message;
import com.maisizhe.modules.message.mapper.MessageMapper;
import com.maisizhe.modules.message.service.MessageService;
import com.maisizhe.modules.message.vo.MessageVO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import com.maisizhe.websocket.handler.WebSocketHandler;
import com.maisizhe.websocket.message.WSMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final RedisUtil redisUtil;
    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送私聊消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendPrivateMessage(Long fromUid, SendMessageDTO dto) {
        // 1. 参数校验
        if (dto.getToUid() == null) {
            throw new BusinessException("接收者ID不能为空");
        }
        
        // 2. 客户端消息ID去重
        String dedupKey = RedisKeyConstants.CLIENT_MSG_ID + dto.getClientMsgId();
        if (redisUtil.hasKey(dedupKey)) {
            log.warn("重复消息: clientMsgId={}", dto.getClientMsgId());
            throw new BusinessException("重复的消息ID");
        }
        redisUtil.setEx(dedupKey, "1", 300); // 5分钟过期
        
        // 3. 生成chat_id(格式: p_小id_大id)
        Long peerId = dto.getToUid();
        String chatId = generatePrivateChatId(fromUid, peerId);
        
        // 4. 生成seq_id(Redis原子自增)
        String seqKey = RedisKeyConstants.SEQ_ID_PREFIX + chatId;
        Long seqId = redisUtil.incr(seqKey);
        
        // 5. 构建消息实体
        Message message = new Message();
        message.setChatId(chatId);
        message.setSeqId(seqId);
        message.setFromUid(fromUid);
        message.setToUid(peerId);
        message.setContent(dto.getContent());
        message.setMsgType(dto.getMsgType());
        message.setMentionedUsers(toJson(dto.getMentionedUsers()));
        message.setIsRecalled(0);
        message.setCreateTime(LocalDateTime.now());
        
        // 6. 写入MySQL
        messageMapper.insert(message);
        
        // 7. 写入Redis ZSet缓存(最近100条)
        cacheMessage(chatId, message);
        
        // 8. 推送给接收者(如果在线)
        pushPrivateMessage(message);
        
        // 9. 返回消息VO
        return convertToVO(message);
    }
    
    /**
     * 发送群聊消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendGroupMessage(Long fromUid, SendMessageDTO dto) {
        // 1. 参数校验
        if (dto.getGroupId() == null) {
            throw new BusinessException("群组ID不能为空");
        }
        
        // 2. 检查群组是否存在
        Group group = groupMapper.selectById(dto.getGroupId());
        if (group == null) {
            throw new BusinessException("群组不存在");
        }
        
        // 3. 检查群组是否已解散
        if (group.getIsDisbanded() != null && group.getIsDisbanded() == 1) {
            throw new BusinessException("群组已解散");
        }
        
        // 4. 检查发送者是否是群组成员
        GroupMember member = groupMemberMapper.selectOne(
            new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, dto.getGroupId())
                .eq(GroupMember::getUserId, fromUid)
                .isNull(GroupMember::getQuitTime)
        );
        if (member == null) {
            throw new BusinessException("不是群成员，无法发送消息");
        }
        
        // 5. 检查是否被禁言
        if (member.getMuteUntil() != null && member.getMuteUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException("您已被禁言，无法发送消息");
        }
        
        // 6. 客户端消息ID去重
        String dedupKey = RedisKeyConstants.CLIENT_MSG_ID + dto.getClientMsgId();
        if (redisUtil.hasKey(dedupKey)) {
            log.warn("重复消息: clientMsgId={}", dto.getClientMsgId());
            throw new BusinessException("重复的消息ID");
        }
        redisUtil.setEx(dedupKey, "1", 300); // 5分钟过期
        
        // 7. 生成chat_id(格式: g_gid)
        String chatId = "g_" + dto.getGroupId();
        
        // 8. 生成seq_id(Redis原子自增)
        String seqKey = RedisKeyConstants.SEQ_ID_PREFIX + chatId;
        Long seqId = redisUtil.incr(seqKey);
        
        // 9. 构建消息实体
        Message message = new Message();
        message.setChatId(chatId);
        message.setSeqId(seqId);
        message.setFromUid(fromUid);
        message.setGroupId(dto.getGroupId());
        message.setContent(dto.getContent());
        message.setMsgType(dto.getMsgType());
        message.setMentionedUsers(toJson(dto.getMentionedUsers()));
        message.setIsRecalled(0);
        message.setCreateTime(LocalDateTime.now());
        
        // 10. 写入MySQL
        messageMapper.insert(message);
        
        // 11. 写入Redis ZSet缓存(最近100条)
        cacheMessage(chatId, message);
        
        // 12. 只推送给群组成员（修复安全漏洞）
        pushGroupMessage(message, dto.getGroupId());
        
        // 13. 返回消息VO
        return convertToVO(message);
    }
    
    /**
     * 获取私聊历史消息
     */
    @Override
    public List<MessageVO> getPrivateHistory(Long userId, Long peerId, Integer limit) {
        // 1. 生成chat_id
        String chatId = generatePrivateChatId(userId, peerId);
        
        // 2. 从Redis ZSet获取最近消息(倒序)
        Object[] messageIds = redisUtil.zRange(
            RedisKeyConstants.CHAT_MSG_PREFIX + chatId, 
            0, 
            limit - 1
        );
        
        // 3. 转换为VO列表
        return Arrays.stream(messageIds)
            .map(id -> {
                Message message = messageMapper.selectById(Long.parseLong(id.toString()));
                return convertToVO(message);
            })
            .toList();
    }
    
    /**
     * 获取群聊历史消息
     */
    @Override
    public List<MessageVO> getGroupHistory(Long groupId, Integer limit) {
        String chatId = "g_" + groupId;
        
        // 从Redis ZSet获取最近消息
        Object[] messageIds = redisUtil.zRange(
            RedisKeyConstants.CHAT_MSG_PREFIX + chatId, 
            0, 
            limit - 1
        );
        
        return Arrays.stream(messageIds)
            .map(id -> {
                Message message = messageMapper.selectById(Long.parseLong(id.toString()));
                return convertToVO(message);
            })
            .toList();
    }
    
    /**
     * 撤回消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long messageId, Long userId) {
        // 1. 查询消息
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        // 2. 权限校验(只有发送者可以撤回)
        if (!message.getFromUid().equals(userId)) {
            throw new BusinessException("无权撤回此消息");
        }
        
        // 3. 时间限制(只能撤回2分钟内的消息)
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
        if (message.getCreateTime().isBefore(twoMinutesAgo)) {
            throw new BusinessException("超过撤回时限");
        }
        
        // 4. 更新消息状态
        message.setIsRecalled(1);
        message.setRecallTime(LocalDateTime.now());
        messageMapper.updateById(message);
        
        // 5. 推送撤回通知
        pushRecallNotification(message);
    }
    
    /**
     * 生成私聊chat_id(保证唯一性)
     */
    private String generatePrivateChatId(Long uid1, Long uid2) {
        Long smallId = Math.min(uid1, uid2);
        Long largeId = Math.max(uid1, uid2);
        return "p_" + smallId + "_" + largeId;
    }
    
    /**
     * 缓存消息到Redis ZSet
     */
    private void cacheMessage(String chatId, Message message) {
        String cacheKey = RedisKeyConstants.CHAT_MSG_PREFIX + chatId;
        redisUtil.zAdd(cacheKey, message.getId(), message.getSeqId());
        
        // 只保留最近100条
        Long count = redisUtil.zCount(cacheKey, 0, Double.MAX_VALUE);
        if (count > 100) {
            // 删除最早的
            Object[] oldMessages = redisUtil.zRange(cacheKey, 0, count - 101);
            redisUtil.zRemove(cacheKey, oldMessages);
        }
    }
    
    /**
     * 推送私聊消息
     */
    private void pushPrivateMessage(Message message) {
        try {
            WSMessage wsMessage = new WSMessage(
                "PRIVATE_MESSAGE",
                convertToVO(message),
                null
            );
            
            // 推送给接收者
            webSocketHandler.sendMessageToUser(message.getToUid(), wsMessage);
            
            // 也推送给发送者(用于多端同步)
            webSocketHandler.sendMessageToUser(message.getFromUid(), wsMessage);
            
        } catch (Exception e) {
            log.error("推送私聊消息失败", e);
        }
    }
    
    /**
     * 推送群聊消息（只发送给群组成员）
     * 
     * @param message 消息实体
     * @param groupId 群组ID
     */
    private void pushGroupMessage(Message message, Long groupId) {
        try {
            WSMessage wsMessage = new WSMessage(
                "GROUP_MESSAGE",
                convertToVO(message),
                null
            );
            
            // 获取群组成员ID列表
            List<GroupMember> members = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>()
                    .eq(GroupMember::getGroupId, groupId)
                    .isNull(GroupMember::getQuitTime)
            );
            
            // 只推送给群组成员
            for (GroupMember member : members) {
                webSocketHandler.sendMessageToUser(member.getUserId(), wsMessage);
            }
            
            log.info("群聊消息推送成功: groupId={}, memberCount={}", groupId, members.size());
            
        } catch (Exception e) {
            log.error("推送群聊消息失败", e);
        }
    }
    
    /**
     * 推送撤回通知
     */
    private void pushRecallNotification(Message message) {
        try {
            WSMessage wsMessage = new WSMessage(
                "SYSTEM_NOTIFICATION",
                "消息已撤回",
                null
            );
            
            if (message.getGroupId() != null) {
                // 群聊撤回通知 - 只发送给群组成员
                List<GroupMember> members = groupMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, message.getGroupId())
                        .isNull(GroupMember::getQuitTime)
                );
                for (GroupMember member : members) {
                    webSocketHandler.sendMessageToUser(member.getUserId(), wsMessage);
                }
                log.info("群聊撤回通知推送成功: groupId={}, memberCount={}", message.getGroupId(), members.size());
            } else {
                // 私聊撤回通知
                webSocketHandler.sendMessageToUser(message.getToUid(), wsMessage);
                webSocketHandler.sendMessageToUser(message.getFromUid(), wsMessage);
            }
            
        } catch (Exception e) {
            log.error("推送撤回通知失败", e);
        }
    }
    
    /**
     * 转换为VO对象
     */
    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        
        // 解析@用户列表
        vo.setMentionedUsers(fromJson(message.getMentionedUsers()));
        
        // 填充发送者信息
        User fromUser = userMapper.selectById(message.getFromUid());
        if (fromUser != null) {
            vo.setFromName(fromUser.getRealName() != null ? fromUser.getRealName() : fromUser.getUsername());
            vo.setFromAvatar(fromUser.getAvatar());
        } else if (message.getFromUid() == 0L) {
            // AI机器人
            vo.setFromName("AI助手");
            vo.setFromAvatar(null);
        } else {
            // 用户不存在，使用默认值
            vo.setFromName("[已注销用户]");
            vo.setFromAvatar(null);
        }
        
        return vo;
    }
    
    /**
     * 对象转JSON字符串
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败", e);
            return null;
        }
    }
    
    /**
     * JSON字符串转对象
     */
    @SuppressWarnings("unchecked")
    private <T> T fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return (T) objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败", e);
            return null;
        }
    }
}
