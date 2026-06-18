package com.maisizhe.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 * 
 * @author MaiSiZhe Team
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    
    /**
     * 文本消息
     */
    TEXT(0, "文本"),
    
    /**
     * 图片消息
     */
    IMAGE(1, "图片"),
    
    /**
     * 文件消息
     */
    FILE(2, "文件"),
    
    /**
     * AI摘要消息(群日报)
     */
    AI_SUMMARY(3, "AI摘要"),
    
    /**
     * AI思考中占位消息
     */
    AI_PLANNING(99, "AI思考中");
    
    /**
     * 类型代码
     */
    private final Integer code;
    
    /**
     * 类型描述
     */
    private final String description;
    
    /**
     * 根据代码获取枚举
     */
    public static MessageTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MessageTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
