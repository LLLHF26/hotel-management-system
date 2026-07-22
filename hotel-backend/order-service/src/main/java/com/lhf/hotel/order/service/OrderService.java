package com.lhf.hotel.order.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.order.model.dto.*;
import com.lhf.hotel.order.model.vo.ProductOrderVO;
import com.lhf.hotel.common.model.vo.ExtraVO;
import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.common.model.vo.PaymentVO;
import com.lhf.hotel.order.model.vo.HotRoomTypeCountVO;

import java.util.List;
import java.util.Map;

public interface OrderService {

    PageResult<OrderVO> list(Integer page, Integer size, String status, String customerPhone,
                             String roomNumber, String checkInDate, String source,
                             String startDate, String endDate);

    OrderVO getById(Long id);

    Map<String, Object> create(OrderCreateDTO dto, String idempotencyKey);

    void cancel(Long id, OrderCancelDTO dto);

    void checkIn(Long id, CheckInDTO dto);

    void checkout(Long id, CheckOutDTO dto);

    Map<String, Object> extend(Long id, ExtendDTO dto);

    Map<String, Object> changeRoom(Long id, ChangeRoomDTO dto);

    Map<String, Object> pay(Long id, PayDTO dto);

    Map<String, Object> refund(Long id, RefundDTO dto);

    List<PaymentVO> getPayments(Long id);

    Map<String, Object> addExtra(Long id, ExtraDTO dto);

    List<ExtraVO> getExtras(Long id);

    void deleteExtra(Long id, Long extraId);

    PageResult<OrderVO> myList(Integer page, Integer size, String status);

    OrderVO myGetById(Long id);

    List<PaymentVO> getPaymentsByTime(String startTime, String endTime);

    List<OrderVO> listByTime(String startDate, String endDate);

    Map<String, Object> getAvailableRooms(Long roomTypeId, String checkIn, String checkOut);

    int autoCancelExpired(int minutes);

    List<HotRoomTypeCountVO> getHotRoomTypeCounts(int topN, int days);

    List<OrderVO> getRoomSchedule(Long roomId, String startDate, String endDate);

    /** 客房商品下单（生成消费项并据积分比例抵扣） */
    Map<String, Object> productOrder(Long id, ProductOrderDTO dto);

    /** 最近客房商品订单（前台配送提醒，关联订单号/房间/客户） */
    PageResult<ProductOrderVO> recentProductOrders(int page, int size);
}
