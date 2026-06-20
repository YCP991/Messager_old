package com.maisizhe.modules.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maisizhe.modules.user.dto.LoginDTO;
import com.maisizhe.modules.user.dto.RegisterDTO;
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
 * 认证控制器集成测试
 * 
 * @author MaiSiZhe Team
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("登录接口集成测试-成功")
    void testLoginIntegrationSuccess() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test");
        loginDTO.setPassword("123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userInfo.username").value("test"))
                .andReturn();

        System.out.println("登录响应: " + result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("登录接口集成测试-用户不存在")
    void testLoginIntegrationUserNotFound() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("登录接口集成测试-密码错误")
    void testLoginIntegrationWrongPassword() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test");
        loginDTO.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("注册接口集成测试-成功")
    void testRegisterIntegrationSuccess() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newtestuser");
        registerDTO.setPassword("123456");
        registerDTO.setRealName("新测试用户");
        registerDTO.setStudentNo("2023211099");
        registerDTO.setClassNo("2311101");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("注册接口集成测试-用户名已存在")
    void testRegisterIntegrationUsernameExists() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("test"); // 使用已存在的用户名
        registerDTO.setPassword("123456");
        registerDTO.setRealName("重复用户");
        registerDTO.setStudentNo("2023211000");
        registerDTO.setClassNo("2311101");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("获取用户信息接口集成测试")
    void testGetUserInfoIntegration() throws Exception {
        // 先登录获取Token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test");
        loginDTO.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        // 从响应中提取token (简化处理，实际应解析JSON)
        
        // 使用用户ID获取信息
        mockMvc.perform(get("/api/auth/user/1")
                .header("Authorization", "Bearer mockToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("登录参数校验测试-用户名为空")
    void testLoginValidationEmptyUsername() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("登录参数校验测试-密码为空")
    void testLoginValidationEmptyPassword() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test");
        loginDTO.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }
}