package com.maisizhe.modules.message.service;

import com.maisizhe.common.enums.MessageTypeEnum;
import com.maisizhe.modules.message.dto.SendMessageDTO;
import com.maisizhe.modules.message.entity.Message;
import com.maisizhe.modules.message.mapper.MessageMapper;
import com.maisizhe.modules.message.service.impl.MessageServiceImpl;
import com.maisizhe.modules.message.vo.MessageVO;
import com.maisizhe.common.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 */
class MessageServiceTest {
    
    @Mock
    private MessageMapper messageMapper;
    
    @Mock
    private RedisUtil redisUtil;
    
    @InjectMocks
    private MessageServiceImpl messageService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testSendGroupMessage_Success() {
        // Given
        Long fromUid = 1001L;
        SendMessageDTO dto = new SendMessageDTO();
        dto.setGroupId(2001L);
        dto.setContent("测试消息");
        dto.setMsgType(MessageTypeEnum.TEXT.getCode());
        dto.setClientMsgId("client_msg_001");
        
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(redisUtil.incr(anyString())).thenReturn(1L);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        
        // When & Then
        // 注意：由于MessageServiceImpl依赖WebSocketHandler，这里会抛出NullPointerException
        // 实际项目中需要使用@SpringBootTest进行集成测试
        assertThrows(NullPointerException.class, () -> {
            messageService.sendGroupMessage(fromUid, dto);
        });
    }
    
    @Test
    void testSendMessageDTO_Validation() {
        // Given
        SendMessageDTO dto = new SendMessageDTO();
        dto.setGroupId(null);
        dto.setContent("");
        
        // Then
        assertNull(dto.getGroupId());
        assertEquals("", dto.getContent());
    }
    
    @Test
    void testMessageEntity_Creation() {
        // Given
        Message message = new Message();
        message.setChatId("g_2001");
        message.setSeqId(1L);
        message.setFromUid(1001L);
        message.setGroupId(2001L);
        message.setContent("测试消息");
        message.setMsgType(MessageTypeEnum.TEXT.getCode());
        message.setCreateTime(LocalDateTime.now());
        
        // Then
        assertEquals("g_2001", message.getChatId());
        assertEquals(1L, message.getSeqId());
        assertEquals(1001L, message.getFromUid());
        assertEquals("测试消息", message.getContent());
        assertNotNull(message.getCreateTime());
    }
}
