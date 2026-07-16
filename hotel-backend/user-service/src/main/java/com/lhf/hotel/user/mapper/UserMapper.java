package com.lhf.hotel.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lhf.hotel.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
