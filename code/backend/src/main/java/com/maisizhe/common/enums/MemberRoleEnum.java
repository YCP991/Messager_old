package com.maisizhe.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群成员角色枚举
 * 
 * @author MaiSiZhe Team
 */
@Getter
@AllArgsConstructor
public enum MemberRoleEnum {
    
    /**
     * 普通成员
     */
    MEMBER(0, "普通成员"),
    
    /**
     * 管理员 - 可踢人、改公告等
     */
    ADMIN(1, "管理员"),
    
    /**
     * 群主 - 最高权限，可转让
     */
    OWNER(2, "群主");
    
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
    public static MemberRoleEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MemberRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
