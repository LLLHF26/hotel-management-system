package com.lhf.hotel.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lhf.hotel.order.model.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    @Select("SELECT room_type_name AS roomTypeName, COUNT(*) AS orderCount " +
            "FROM orders " +
            "WHERE is_deleted = 0 AND create_time >= #{startDate} " +
            "GROUP BY room_type_name " +
            "ORDER BY orderCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectHotRoomTypeCounts(@Param("startDate") LocalDateTime startDate,
                                                      @Param("limit") int limit);
}
