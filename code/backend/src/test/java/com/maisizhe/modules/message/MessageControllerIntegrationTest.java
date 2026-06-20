package com.maisizhe.modules.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maisizhe.modules.message.dto.SendMessageDTO;
import com.maisizhe.modules.user.dto.LoginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息控制器集成测试
 * 
 * @author MaiSiZhe Team
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // 登录获取Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test");
        loginDTO.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andReturn();

        // 从响应中提取token (简化处理)
        String response = loginResult.getResponse().getContentAsString();
        if (response.contains("token")) {
            authToken = "Bearer mockToken";
        }
    }

    @Test
    @DisplayName("发送消息集成测试-成功")
    void testSendMessageIntegrationSuccess() throws Exception {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setGroupId(1L);
        sendMessageDTO.setContent("集成测试消息");
        sendMessageDTO.setMsgType(0);

        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendMessageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("集成测试消息"));
    }

    @Test
    @DisplayName("发送消息集成测试-图片类型")
    void testSendMessageIntegrationImage() throws Exception {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setGroupId(1L);
        sendMessageDTO.setContent("http://example.com/image.jpg");
        sendMessageDTO.setMsgType(1); // 图片消息

        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendMessageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("发送消息集成测试-文件类型")
    void testSendMessageIntegrationFile() throws Exception {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setGroupId(1L);
        sendMessageDTO.setContent("http://example.com/file.pdf");
        sendMessageDTO.setMsgType(2); // 文件消息

        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendMessageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("发送消息集成测试-AI消息")
    void testSendMessageIntegrationAI() throws Exception {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setGroupId(1L);
        sendMessageDTO.setContent("@AI 请帮我分析这个问题");
        sendMessageDTO.setMsgType(0);

        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendMessageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取群组消息历史集成测试")
    void testGetGroupMessagesIntegration() throws Exception {
        mockMvc.perform(get("/api/messages/group/1")
                .header("Authorization", authToken)
                .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取群组消息历史集成测试-带游标")
    void testGetGroupMessagesIntegrationWithCursor() throws Exception {
        mockMvc.perform(get("/api/messages/group/1")
                .header("Authorization", authToken)
                .param("cursor", "100")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取消息详情集成测试")
    void testGetMessageByIdIntegration() throws Exception {
        mockMvc.perform(get("/api/messages/1")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("撤回消息集成测试")
    void testRecallMessageIntegration() throws Exception {
        mockMvc.perform(delete("/api/messages/1/recall")
                .header("Authorization", authToken)
                .param("groupId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取未读消息数集成测试")
    void testGetUnreadCountIntegration() throws Exception {
        mockMvc.perform(get("/api/messages/unread")
                .header("Authorization", authToken)
                .param("groupId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("发送消息参数校验测试-内容为空")
    void testSendMessageValidationEmptyContent() throws Exception {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setGroupId(1L);
        sendMessageDTO.setContent("");
        sendMessageDTO.setMsgType(0);

        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendMessageDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("发送消息参数校验测试-群组ID为空")
    void testSendMessageValidationEmptyGroupId() throws Exception {
        SendMessageDTO sendMessageDTO = new SendMessageDTO();
        sendMessageDTO.setGroupId(null);
        sendMessageDTO.setContent("测试内容");
        sendMessageDTO.setMsgType(0);

        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendMessageDTO)))
                .andExpect(status().isBadRequest());
    }
}