package com.maisizhe.modules.user.controller;

import com.maisizhe.common.response.Result;
import com.maisizhe.modules.user.dto.LoginDTO;
import com.maisizhe.modules.user.dto.RegisterDTO;
import com.maisizhe.modules.user.service.UserService;
import com.maisizhe.modules.user.vo.LoginVO;
import com.maisizhe.modules.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关接口
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户登录
     * 
     * @param dto 登录请求
     * @return 登录响应(包含Token和用户信息)
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        log.info("用户登录: {}", dto.getUsername());
        LoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }
    
    /**
     * 用户注册
     * 
     * @param dto 注册请求
     * @return 成功响应
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        log.info("用户注册: {}", dto.getUsername());
        userService.register(dto);
        return Result.success();
    }
    
    /**
     * 获取当前用户信息
     * 
     * @param userId 用户ID(从JWT中获取)
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    public Result<UserVO> getUserInfo(@PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }
    
    /**
     * 更新用户资料
     * 
     * @param userId 用户ID(从JWT中获取)
     * @param userVO 用户信息
     * @return 成功响应
     */
    @PutMapping("/user/{userId}")
    public Result<Void> updateProfile(
            @PathVariable Long userId,
            @RequestBody UserVO userVO) {
        userService.updateProfile(userId, userVO);
        return Result.success();
    }
}
