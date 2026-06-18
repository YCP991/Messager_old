package com.maisizhe.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 * 
 * @author MaiSiZhe Team
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {
    
    /**
     * 学生
     */
    STUDENT(0, "学生"),
    
    /**
     * 教师
     */
    TEACHER(1, "教师"),
    
    /**
     * 管理员
     */
    ADMIN(2, "管理员");
    
    /**
     * 角色代码
     */
    private final Integer code;
    
    /**
     * 角色描述
     */
    private final String description;
    
    /**
     * 根据代码获取枚举
     */
    public static RoleEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
