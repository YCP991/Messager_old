package com.maisizhe.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maisizhe.common.exception.BusinessException;
import com.maisizhe.modules.user.dto.LoginDTO;
import com.maisizhe.modules.user.dto.RegisterDTO;
import com.maisizhe.modules.user.entity.User;
import com.maisizhe.modules.user.mapper.UserMapper;
import com.maisizhe.modules.user.service.UserService;
import com.maisizhe.modules.user.vo.LoginVO;
import com.maisizhe.modules.user.vo.UserVO;
import com.maisizhe.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 
 * @author MaiSiZhe Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 2. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 3. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 5. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 6. 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        
        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        userInfo.setClassName(user.getClassNo());
        loginVO.setUserInfo(userInfo);
        
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        
        return loginVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 2. 检查学号是否已存在
        count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStudentNo, dto.getStudentNo()));
        if (count > 0) {
            throw new BusinessException("学号已被注册");
        }
        
        // 3. 创建用户
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        
        // 4. 加密密码
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // 5. 设置默认值
        user.setStatus(1); // 正常状态
        if (user.getRole() == null) {
            user.setRole(0); // 默认学生
        }
        
        // 6. 保存用户
        userMapper.insert(user);
        
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }
    
    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        return userVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserVO userVO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新允许修改的字段
        user.setRealName(userVO.getRealName());
        user.setEmail(userVO.getEmail());
        user.setPhone(userVO.getPhone());
        user.setAvatar(userVO.getAvatar());
        
        userMapper.updateById(user);
        
        log.info("用户资料更新成功: userId={}", userId);
    }
}
