package com.maisizhe.modules.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新好友排序请求DTO
 * 
 * @author MaiSiZhe Team
 */
@Data
public class UpdateFriendSortDTO {
    
    /**
     * 好友ID
     */
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
    
    /**
     * 排序权重
     */
    @NotNull(message = "排序权重不能为空")
    private Double sortOrder;
    
    /**
     * 是否置顶:0-否,1-是
     */
    @NotNull(message = "置顶状态不能为空")
    private Integer isPinned;
}
