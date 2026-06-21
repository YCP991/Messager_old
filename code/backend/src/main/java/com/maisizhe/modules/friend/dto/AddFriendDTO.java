package com.maisizhe.modules.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加好友请求DTO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class AddFriendDTO {
    
    /**
     * 目标用户ID
     */
    @NotNull(message = "目标用户ID不能为空")
    private Long targetUserId;
    
    /**
     * 备注名(可选)
     */
    private String remark;
}
