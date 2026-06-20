package com.maisizhe.modules.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maisizhe.modules.user.dto.LoginDTO;
import com.maisizhe.modules.user.dto.RegisterDTO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import com.maisizhe.modules.user.service.impl.UserServiceImpl;
import com.maisizhe.modules.user.vo.LoginVO;
import com.maisizhe.modules.user.vo.UserVO;
import com.maisizhe.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 * 
 * @author MaiSiZhe Team
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private LoginDTO loginDTO;
    private RegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRealName("测试用户");
        testUser.setStudentNo("2023211001");
        testUser.setClassNo("2311101");
        testUser.setRole(0);
        testUser.setStatus(1);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");

        registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("123456");
        registerDTO.setRealName("新用户");
        registerDTO.setStudentNo("2023211002");
        registerDTO.setClassNo("2311101");
    }

    @Test
    @DisplayName("登录成功测试")
    void testLoginSuccess() {
        // Mock行为 - 使用selectOne和LambdaQueryWrapper
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("123456", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser", 0)).thenReturn("mockToken");

        // 执行测试
        LoginVO result = userService.login(loginDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("mockToken", result.getToken());
        assertEquals("testuser", result.getUserInfo().getUsername());

        // 验证方法调用
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(passwordEncoder).matches("123456", "encodedPassword");
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("登录失败-用户不存在")
    void testLoginFailUserNotFound() {
        // Mock行为
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });

        // 验证方法调用
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("登录失败-密码错误")
    void testLoginFailWrongPassword() {
        // Mock行为
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // 修改密码为错误密码
        loginDTO.setPassword("wrongpassword");

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });

        // 验证方法调用
        verify(userMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
    }

    @Test
    @DisplayName("注册成功测试")
    void testRegisterSuccess() {
        // Mock行为 - 用户名和学号都不存在
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行测试
        userService.register(registerDTO);

        // 验证方法调用
        verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
        verify(passwordEncoder).encode("123456");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败-用户名已存在")
    void testRegisterFailUsernameExists() {
        // Mock行为 - 用户名已存在
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.register(registerDTO);
        });

        // 验证方法调用
        verify(userMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("获取用户信息成功")
    void testGetUserByIdSuccess() {
        // Mock行为
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        UserVO result = userService.getUserById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getRealName());

        // 验证方法调用
        verify(userMapper).selectById(1L);
    }

    @Test
    @DisplayName("获取用户信息失败-用户不存在")
    void testGetUserByIdFailNotFound() {
        // Mock行为
        when(userMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.getUserById(999L);
        });

        // 验证方法调用
        verify(userMapper).selectById(999L);
    }
}