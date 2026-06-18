package com.maisizhe.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求DTO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class RegisterDTO {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 学号/工号
     */
    @NotBlank(message = "学号不能为空")
    private String studentNo;
    
    /**
     * 真实姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    
    /**
     * 班级号
     */
    private String classNo;
    
    /**
     * 院系
     */
    private String department;
    
    /**
     * 角色(0-学生,1-教师)
     */
    private Integer role = 0;
}
