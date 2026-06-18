package com.maisizhe.common.exception;

import lombok.Getter;

/**
 * 群组权限异常类
 * 
 * @author MaiSiZhe Team
 */
@Getter
public class GroupPermissionException extends BusinessException {
    
    public GroupPermissionException(String message) {
        super(403, message);
    }
}
