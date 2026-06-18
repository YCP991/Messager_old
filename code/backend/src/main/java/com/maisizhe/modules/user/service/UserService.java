package com.maisizhe.modules.user.service;

import com.maisizhe.modules.user.dto.LoginDTO;
import com.maisizhe.modules.user.dto.RegisterDTO;
import com.maisizhe.modules.user.vo.LoginVO;
import com.maisizhe.modules.user.vo.UserVO;

/**
 * 用户服务接口
 * 
 * @author MaiSiZhe Team
 */
public interface UserService {
    
    /**
     * 用户登录
     * 
     * @param dto 登录请求
     * @return 登录响应(包含Token)
     */
    LoginVO login(LoginDTO dto);
    
    /**
     * 用户注册
     * 
     * @param dto 注册请求
     */
    void register(RegisterDTO dto);
    
    /**
     * 根据ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);
    
    /**
     * 更新用户资料
     * 
     * @param userId 用户ID
     * @param userVO 用户信息
     */
    void updateProfile(Long userId, UserVO userVO);
}
