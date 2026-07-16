package com.lhf.hotel.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lhf.hotel.order.model.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
