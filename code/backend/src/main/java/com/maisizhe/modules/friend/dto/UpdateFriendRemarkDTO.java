package com.maisizhe.modules.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新好友备注请求DTO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class UpdateFriendRemarkDTO {
    
    /**
     * 好友ID
     */
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
    
    /**
     * 新备注名
     */
    private String remark;
}
