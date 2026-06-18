package com.maisizhe.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群组类型枚举
 * 
 * @author MaiSiZhe Team
 */
@Getter
@AllArgsConstructor
public enum GroupTypeEnum {
    
    /**
     * 普通群 - 任何人可创建
     */
    NORMAL(0, "普通群", "normalGroupStrategy"),
    
    /**
     * 班级群 - 仅本班学生可创建，自动拉入全班
     */
    CLASS(1, "班级群", "classGroupStrategy"),
    
    /**
     * 课程群 - 教师创建，自动拉入选课学生
     */
    COURSE(2, "课程群", "courseGroupStrategy");
    
    /**
     * 类型代码
     */
    private final Integer code;
    
    /**
     * 类型描述
     */
    private final String description;
    
    /**
     * 策略名称(用于工厂模式)
     */
    private final String strategyName;
    
    /**
     * 根据代码获取枚举
     */
    public static GroupTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GroupTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
