package com.maisizhe.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maisizhe.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * @author MaiSiZhe Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
