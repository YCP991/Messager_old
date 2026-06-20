package com.maisizhe.modules.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maisizhe.common.util.RedisUtil;
import com.maisizhe.modules.group.entity.Group;
import com.maisizhe.modules.group.entity.GroupMember;
import com.maisizhe.modules.group.mapper.GroupMapper;
import com.maisizhe.modules.group.mapper.GroupMemberMapper;
import com.maisizhe.modules.message.dto.SendMessageDTO;
import com.maisizhe.modules.message.entity.Message;
import com.maisizhe.modules.message.mapper.MessageMapper;
import com.maisizhe.modules.message.service.impl.MessageServiceImpl;
import com.maisizhe.modules.message.vo.MessageVO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import com.maisizhe.websocket.handler.WebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 * 
 * @author MaiSiZhe Team
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private WebSocketHandler webSocketHandler;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message testMessage;
    private Group testGroup;
    private GroupMember testMember;
    private User testUser;
    private SendMessageDTO sendMessageDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试消息
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId("g_1");
        testMessage.setSeqId(1L);
        testMessage.setFromUid(1L);
        testMessage.setGroupId(1L);
        testMessage.setContent("测试消息内容");
        testMessage.setMsgType(0); // 文本消息
        testMessage.setIsRecalled(0);
        testMessage.setCreateTime(LocalDateTime.now());

        // 初始化测试群组
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setGroupName("测试群组");
        testGroup.setMemberCount(5);

        // 初始化测试成员
        testMember = new GroupMember();
        testMember.setId(1L);
        testMember.setGroupId(1L);
        testMember.setUserId(1L);
        testMember.setRole(2); // 群主
        testMember.setJoinTime(LocalDateTime.now());

        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setAvatar("avatar.jpg");

        // 初始化发送消息DTO
        sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setGroupId(1L);
        sendMessageDTO.setContent("新消息内容");
        sendMessageDTO.setMsgType(0);
    }

    @Test
    @DisplayName("发送群聊消息成功测试")
    void testSendGroupMessageSuccess() {
        // Mock行为 - 群组存在、未解散
        when(groupMapper.selectById(1L)).thenReturn(testGroup);
        
        // Mock行为 - 用户是群成员
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMember);
        
        // Mock Redis操作
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(redisUtil.incr(anyString())).thenReturn(1L);
        
        // Mock消息操作
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        
        // Mock用户查询
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        
        // Mock获取群成员列表（用于推送消息）
        when(groupMemberMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(testMember));
        
        // 执行测试
        MessageVO result = messageService.sendGroupMessage(1L, sendMessageDTO);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("新消息内容", result.getContent());
        
        // 验证方法调用
        verify(groupMapper).selectById(1L);
        verify(groupMemberMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(messageMapper).insert(any(Message.class));
        verify(groupMemberMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("发送群聊消息失败-群组ID为空")
    void testSendGroupMessageFailEmptyGroupId() {
        // 设置空的groupId
        sendMessageDTO.setGroupId(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.sendGroupMessage(1L, sendMessageDTO);
        });
    }

    @Test
    @DisplayName("发送群聊消息失败-群组不存在")
    void testSendGroupMessageFailGroupNotFound() {
        // Mock行为 - 群组不存在
        when(groupMapper.selectById(1L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.sendGroupMessage(1L, sendMessageDTO);
        });
        
        // 验证方法调用
        verify(groupMapper).selectById(1L);
        verify(messageMapper, never()).insert(any(Message.class));
    }

    @Test
    @DisplayName("发送群聊消息失败-群组已解散")
    void testSendGroupMessageFailGroupDisbanded() {
        // 设置群组为已解散状态
        testGroup.setIsDisbanded(1);
        when(groupMapper.selectById(1L)).thenReturn(testGroup);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.sendGroupMessage(1L, sendMessageDTO);
        });
        
        // 验证方法调用
        verify(groupMapper).selectById(1L);
        verify(messageMapper, never()).insert(any(Message.class));
    }

    @Test
    @DisplayName("发送群聊消息失败-不是群成员")
    void testSendGroupMessageFailNotMember() {
        // Mock行为 - 群组存在
        when(groupMapper.selectById(1L)).thenReturn(testGroup);
        // Mock行为 - 用户不是群成员
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.sendGroupMessage(1L, sendMessageDTO);
        });
        
        // 验证方法调用
        verify(groupMapper).selectById(1L);
        verify(groupMemberMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(messageMapper, never()).insert(any(Message.class));
    }

    @Test
    @DisplayName("发送群聊消息失败-用户被禁言")
    void testSendGroupMessageFailUserMuted() {
        // 设置用户被禁言
        testMember.setMuteUntil(LocalDateTime.now().plusMinutes(30));
        
        // Mock行为
        when(groupMapper.selectById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMember);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.sendGroupMessage(1L, sendMessageDTO);
        });
        
        // 验证未发送消息
        verify(messageMapper, never()).insert(any(Message.class));
    }

    @Test
    @DisplayName("获取群聊历史消息成功")
    void testGetGroupHistorySuccess() {
        // Mock行为 - 从Redis获取消息ID
        when(redisUtil.zRange(anyString(), anyLong(), anyLong()))
                .thenReturn(new Object[]{"1"});
        when(messageMapper.selectById(1L)).thenReturn(testMessage);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        List<MessageVO> result = messageService.getGroupHistory(1L, 20);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证方法调用
        verify(redisUtil).zRange(anyString(), anyLong(), anyLong());
        verify(messageMapper).selectById(1L);
    }

    @Test
    @DisplayName("获取群聊历史消息-空列表")
    void testGetGroupHistoryEmpty() {
        // Mock行为 - Redis返回空
        when(redisUtil.zRange(anyString(), anyLong(), anyLong()))
                .thenReturn(new Object[]{});

        // 执行测试
        List<MessageVO> result = messageService.getGroupHistory(1L, 20);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("撤回消息成功")
    void testRecallMessageSuccess() {
        // 创建可撤回的消息(2分钟内)
        Message recentMessage = new Message();
        recentMessage.setId(1L);
        recentMessage.setFromUid(1L);
        recentMessage.setContent("可撤回消息");
        recentMessage.setCreateTime(LocalDateTime.now().minusMinutes(1));
        recentMessage.setIsRecalled(0);

        // Mock行为
        when(messageMapper.selectById(1L)).thenReturn(recentMessage);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        // 执行测试
        messageService.recallMessage(1L, 1L);

        // 验证方法调用
        verify(messageMapper).selectById(1L);
        verify(messageMapper).updateById(any(Message.class));
    }

    @Test
    @DisplayName("撤回消息失败-超过2分钟")
    void testRecallMessageFailTimeout() {
        // 创建超过2分钟的消息
        Message oldMessage = new Message();
        oldMessage.setId(1L);
        oldMessage.setFromUid(1L);
        oldMessage.setContent("超时消息");
        oldMessage.setCreateTime(LocalDateTime.now().minusMinutes(5));
        oldMessage.setIsRecalled(0);

        // Mock行为
        when(messageMapper.selectById(1L)).thenReturn(oldMessage);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.recallMessage(1L, 1L);
        });

        // 验证未执行更新
        verify(messageMapper, never()).updateById(any(Message.class));
    }

    @Test
    @DisplayName("撤回消息失败-不是发送者")
    void testRecallMessageFailNotSender() {
        // Mock行为
        when(messageMapper.selectById(1L)).thenReturn(testMessage);
        // 发送者是1L，但操作者是2L

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.recallMessage(1L, 2L);
        });

        // 验证未执行更新
        verify(messageMapper, never()).updateById(any(Message.class));
    }

    @Test
    @DisplayName("发送私聊消息成功测试")
    void testSendPrivateMessageSuccess() {
        // 设置私聊参数
        sendMessageDTO.setGroupId(null);
        sendMessageDTO.setToUid(2L);

        // Mock行为
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(redisUtil.incr(anyString())).thenReturn(1L);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(userMapper.selectById(anyLong())).thenReturn(testUser);

        // 执行测试
        MessageVO result = messageService.sendPrivateMessage(1L, sendMessageDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("新消息内容", result.getContent());

        // 验证方法调用
        verify(messageMapper).insert(any(Message.class));
    }

    @Test
    @DisplayName("获取私聊历史消息成功")
    void testGetPrivateHistorySuccess() {
        // Mock行为 - 从Redis获取消息ID
        when(redisUtil.zRange(anyString(), anyLong(), anyLong()))
                .thenReturn(new Object[]{"1"});
        when(messageMapper.selectById(1L)).thenReturn(testMessage);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        List<MessageVO> result = messageService.getPrivateHistory(1L, 2L, 20);

        // 验证结果
        assertNotNull(result);

        // 验证方法调用
        verify(redisUtil).zRange(anyString(), anyLong(), anyLong());
        verify(messageMapper).selectById(1L);
    }
}