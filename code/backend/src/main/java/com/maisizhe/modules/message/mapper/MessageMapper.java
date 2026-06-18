package com.maisizhe.modules.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maisizhe.modules.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper接口
 * 
 * @author MaiSiZhe Team
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
