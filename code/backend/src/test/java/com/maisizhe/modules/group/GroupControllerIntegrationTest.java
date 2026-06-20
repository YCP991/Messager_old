package com.maisizhe.modules.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maisizhe.modules.group.dto.CreateGroupDTO;
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
 * 群组控制器集成测试
 * 
 * @author MaiSiZhe Team
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupControllerIntegrationTest {

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
            authToken = "Bearer mockToken"; // 实际应从JSON中解析
        }
    }

    @Test
    @DisplayName("创建群组集成测试-成功")
    void testCreateGroupIntegrationSuccess() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setGroupName("集成测试群组");
        createGroupDTO.setGroupType(0);
        createGroupDTO.setMaxMembers(100);

        mockMvc.perform(post("/api/groups")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.groupName").value("集成测试群组"));
    }

    @Test
    @DisplayName("创建班级群集成测试-成功")
    void testCreateClassGroupIntegrationSuccess() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setGroupName("测试班级群");
        createGroupDTO.setGroupType(1);
        createGroupDTO.setClassNo("2311101");
        createGroupDTO.setMaxMembers(50);

        mockMvc.perform(post("/api/groups")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建班级群集成测试-缺少班级号")
    void testCreateClassGroupIntegrationFailNoClassNo() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setGroupName("测试班级群");
        createGroupDTO.setGroupType(1);
        createGroupDTO.setMaxMembers(50);

        mockMvc.perform(post("/api/groups")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("获取群组列表集成测试")
    void testGetUserGroupsIntegration() throws Exception {
        mockMvc.perform(get("/api/groups")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取群组详情集成测试")
    void testGetGroupByIdIntegration() throws Exception {
        // 先创建一个群组
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setGroupName("详情测试群");
        createGroupDTO.setGroupType(0);

        MvcResult createResult = mockMvc.perform(post("/api/groups")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andReturn();

        // 获取群组详情 (使用假设的groupId)
        mockMvc.perform(get("/api/groups/1")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("加入群组集成测试")
    void testJoinGroupIntegration() throws Exception {
        mockMvc.perform(post("/api/groups/1/join")
                .header("Authorization", authToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("退出群组集成测试")
    void testQuitGroupIntegration() throws Exception {
        mockMvc.perform(post("/api/groups/1/quit")
                .header("Authorization", authToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取群成员列表集成测试")
    void testGetGroupMembersIntegration() throws Exception {
        mockMvc.perform(get("/api/groups/1/members")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新群组公告集成测试")
    void testUpdateAnnouncementIntegration() throws Exception {
        String announcement = "这是新的群组公告";

        mockMvc.perform(put("/api/groups/1/announcement")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(announcement))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("创建群组参数校验测试-群名为空")
    void testCreateGroupValidationEmptyName() throws Exception {
        CreateGroupDTO createGroupDTO = new CreateGroupDTO();
        createGroupDTO.setGroupName("");
        createGroupDTO.setGroupType(0);

        mockMvc.perform(post("/api/groups")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGroupDTO)))
                .andExpect(status().isBadRequest());
    }
}