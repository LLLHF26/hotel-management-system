package com.lhf.hotel.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.dto.RoomStatusChangeDTO;
import com.lhf.hotel.common.enums.*;
import com.lhf.hotel.common.event.dto.*;
import com.lhf.hotel.common.exception.BusinessException;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.SnowflakeIdUtil;

import com.lhf.hotel.common.model.vo.RefundRecordVO;
import com.lhf.hotel.order.feign.financeService.FinanceRefundFeignClient;
import com.lhf.hotel.order.feign.roomServiceFeignClient.RoomFeignClient;
import com.lhf.hotel.order.feign.roomServiceFeignClient.RoomTypeFeignClient;
import com.lhf.hotel.order.feign.userService.UserFeignClient;
import com.lhf.hotel.order.mapper.OrderExtraMapper;
import com.lhf.hotel.order.mapper.OrdersMapper;
import com.lhf.hotel.order.mapper.PaymentMapper;
import com.lhf.hotel.order.model.dto.*;
import com.lhf.hotel.order.model.entity.OrderExtra;
import com.lhf.hotel.order.model.entity.Orders;
import com.lhf.hotel.order.model.entity.Payment;
import com.lhf.hotel.common.util.UserContext;
import com.lhf.hotel.common.model.vo.ExtraVO;
import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.common.model.vo.PaymentVO;
import com.lhf.hotel.order.service.OrderService;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import com.lhf.hotel.room.model.vo.RoomVO;
import com.lhf.hotel.user.model.vo.CustomerVO;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderExtraMapper orderExtraMapper;
    private final PaymentMapper paymentMapper;

    // feign调用room-service服务接口
    @Resource
    private RoomFeignClient roomFeignClient;
    @Resource
    private RoomTypeFeignClient roomTypeFeignClient;

    // feign调用user-service服务接口
    @Resource
    private UserFeignClient userFeignClient;

    // feign调用finance-service服务接口
    @Resource
    private FinanceRefundFeignClient financeRefundFeignClient;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResult<OrderVO> list(Integer page, Integer size, String status, String customerPhone,
                                    String roomNumber, String checkInDate, String source,
                                    String startDate, String endDate) {
        // PM分页
        IPage<Orders> orderPage = new Page<>(page, size);
        // 查询
        orderPage = ordersMapper.selectPage(orderPage, new QueryWrapper<Orders>()
                .eq(status != null, "status", status)
                .eq(customerPhone != null, "customer_phone", customerPhone)
                .eq(roomNumber != null, "room_number", roomNumber)
                .eq(checkInDate != null, "check_in_date", checkInDate)
                .eq(source != null, "source", source)
                .ge(startDate != null, "check_in_date", startDate)
                .le(endDate != null, "check_in_date", endDate)
         );
        // 转换封装
        List<OrderVO> orderVOS = BeanUtil.copyToList(orderPage.getRecords(), OrderVO.class);
        return PageResult.of(orderPage.getTotal(), page, size, orderVOS);
    }

    @Override
    public OrderVO getById(Long id) {
        OrderVO orderVO = OrderVO.builder().build();
        // 查询订单信息
        Orders order = ordersMapper.selectById(id);
        // 封装订单信息
        BeanUtil.copyProperties(order, orderVO);
        // 查询订单id相关的详细信息
        List<OrderExtra> orderExtras = orderExtraMapper.selectList(new QueryWrapper<OrderExtra>().eq("order_id", id));
        // 封装订单详情
        orderVO.setExtras(BeanUtil.copyToList(orderExtras, ExtraVO.class));
        return orderVO;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Map<String, Object> create(OrderCreateDTO dto) {
        // 1. 校验 checkOutDate > checkInDate
        LocalDate checkIn = LocalDate.parse(dto.getCheckInDate());
        LocalDate checkOut = LocalDate.parse(dto.getCheckOutDate());
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessException(400, "退房日期必须晚于入住日期");
        }

        // 2. 确定预订房间数
        int roomCount = dto.getRoomCount() != null && dto.getRoomCount() > 0 ? dto.getRoomCount() : 1;

        // 3. 根据房型查找空闲房间
        Result<PageResult<RoomVO>> listResult = roomFeignClient.list(1, roomCount, dto.getRoomTypeId(), "空闲中", null, null);
        PageResult<RoomVO> pageResult = listResult.getData();
        if (pageResult == null || pageResult.getRecords() == null || pageResult.getRecords().size() < roomCount) {
            throw new BusinessException(400, "该房型仅剩" + (pageResult != null && pageResult.getRecords() != null ? pageResult.getRecords().size() : 0) + "间可用，无法预订" + roomCount + "间");
        }
        List<RoomVO> availableRooms = pageResult.getRecords().subList(0, roomCount);

        // 4. 锁定所有房间并逐个创建订单
        List<Long> lockedRoomIds = new ArrayList<>();
        for (RoomVO room : availableRooms) {
            Long roomId = room.getId();
            String lockKey = "lock:room:" + roomId;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean isLock = lock.tryLock(0, 30, TimeUnit.SECONDS);
                if (!isLock) {
                    // 释放已锁定的房间
                    unlockRooms(lockedRoomIds);
                    throw new BusinessException(400, "房间 " + room.getRoomNumber() + " 正在预订中，请稍后重试");
                }
                lockedRoomIds.add(roomId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                unlockRooms(lockedRoomIds);
                throw new BusinessException(500, "预订失败");
            }
        }

        // 5. 确定会员ID
        Long customerId = dto.getCustomerId() != null ? dto.getCustomerId() : UserContext.getUserId();
        if (customerId == null) {
            unlockRooms(lockedRoomIds);
            throw new BusinessException(400, "会员不能为空");
        }

        Result<CustomerVO> customerVOResult = userFeignClient.detail(customerId);
        CustomerVO customer = customerVOResult.getData();
        Result<RoomTypeVO> roomTypeResult = roomTypeFeignClient.detail(dto.getRoomTypeId());
        RoomTypeVO roomType = roomTypeResult.getData();
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

        // 6. 为每间房创建订单
        Long firstOrderId = null;
        String firstOrderNo = null;
        for (RoomVO roomVO : availableRooms) {
            Long roomId = roomVO.getId();

            // 加锁后再次确认房间状态
            Result<RoomVO> detailResult = roomFeignClient.detail(roomId);
            RoomVO currentRoom = detailResult.getData();
            if (currentRoom == null || !RoomStatus.空闲中.name().equals(currentRoom.getStatus())) {
                unlockRooms(lockedRoomIds);
                String rn = currentRoom != null ? currentRoom.getRoomNumber() : roomId.toString();
                throw new BusinessException(400, "房间 " + rn + " 已被预订");
            }

            RoomStatusChangeDTO changeDTO = new RoomStatusChangeDTO(RoomStatus.预订中.name(), "订单创建");
            roomFeignClient.changeStatus(roomId, changeDTO);

            String orderNo = SnowflakeIdUtil.generateOrderNo();
            Orders order = new Orders();
            order.setOrderNo(orderNo);
            order.setCustomerId(customerId);
            String guestName = dto.getGuestName() != null && !dto.getGuestName().isBlank()
                    ? dto.getGuestName() : (customer != null ? customer.getRealName() : "");
            String guestPhone = dto.getGuestPhone() != null && !dto.getGuestPhone().isBlank()
                    ? dto.getGuestPhone() : (customer != null ? customer.getPhone() : "");
            order.setCustomerName(guestName);
            order.setCustomerPhone(guestPhone);
            order.setRoomId(roomId);
            order.setRoomNumber(currentRoom.getRoomNumber());
            order.setRoomTypeName(currentRoom.getRoomTypeName());
            order.setCheckInDate(checkIn);
            order.setCheckOutDate(checkOut);
            order.setNights(nights);
            order.setRoomPrice(roomType.getPrice());
            order.setRoomTotal(roomType.getPrice().multiply(new BigDecimal(nights)));
            order.setExtraTotal(BigDecimal.ZERO);
            order.setTotalAmount(roomType.getPrice().multiply(new BigDecimal(nights)));
            order.setPaidAmount(BigDecimal.ZERO);
            order.setStatus(OrderStatus.待支付.name());
            order.setSource(OrderSource.ONLINE.name());
            order.setRemark(dto.getRemark());
            ordersMapper.insert(order);

            if (firstOrderId == null) {
                firstOrderId = order.getId();
                firstOrderNo = orderNo;
            }
        }

        // 释放所有锁
        unlockRooms(lockedRoomIds);

        BigDecimal totalAmount = roomType.getPrice().multiply(new BigDecimal(nights)).multiply(new BigDecimal(roomCount));

        // 发布事件（为每间房的订单发布创建事件）
        for (RoomVO roomVO : availableRooms) {
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(firstOrderId).orderNo(firstOrderNo)
                .eventType(OrderEventType.CREATED).status(OrderStatus.待支付.name())
                .customerId(customerId).customerName(customer != null ? customer.getRealName() : "")
                .roomId(roomVO.getId()).roomNumber(roomVO.getRoomNumber())
                .checkInDate(checkIn).checkOutDate(checkOut).nights(nights)
                .totalAmount(roomType.getPrice().multiply(new BigDecimal(nights)))
                .timestamp(LocalDateTime.now()).build();
            publishEvent(event, "order.created");
        }

        return Map.of("id", firstOrderId, "orderNo", firstOrderNo, "roomCount", roomCount, "totalAmount", totalAmount);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void cancel(Long id, OrderCancelDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!OrderStatus.待支付.name().equals(order.getStatus())) {
            throw new BusinessException(400, "订单当前状态不可取消");
        }
        order.setStatus(OrderStatus.已取消.name());
        ordersMapper.updateById(order);
        roomFeignClient.changeStatus(order.getRoomId(), new RoomStatusChangeDTO(RoomStatus.空闲中.name(), "订单取消"));

        OrderCancelledEvent event = OrderCancelledEvent.builder()
            .orderId(order.getId()).orderNo(order.getOrderNo())
            .eventType(OrderEventType.CANCELLED).status(OrderStatus.已取消.name())
            .customerId(order.getCustomerId()).customerName(order.getCustomerName())
            .roomId(order.getRoomId()).roomNumber(order.getRoomNumber())
            .cancelReason(dto.getReason())
            .timestamp(LocalDateTime.now()).build();
        publishEvent(event, "order.cancelled");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void checkIn(Long id, CheckInDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!OrderStatus.已支付.name().equals(order.getStatus())) {
            throw new BusinessException(400, "订单当前状态不可办理入住");
        }
        order.setActualCheckIn(LocalDateTime.now());
        order.setStatus(OrderStatus.已入住.name());
        if (dto.getDeposit() != null) {
            order.setDeposit(dto.getDeposit());
        }
        ordersMapper.updateById(order);
        roomFeignClient.changeStatus(order.getRoomId(), new RoomStatusChangeDTO(RoomStatus.入住中.name(), "办理入住"));

        OrderCheckInEvent event = OrderCheckInEvent.builder()
            .orderId(order.getId()).orderNo(order.getOrderNo())
            .eventType(OrderEventType.CHECK_IN).status(OrderStatus.已入住.name())
            .customerId(order.getCustomerId()).customerName(order.getCustomerName())
            .roomId(order.getRoomId()).roomNumber(order.getRoomNumber())
            .actualCheckIn(order.getActualCheckIn()).deposit(order.getDeposit())
            .timestamp(LocalDateTime.now()).build();
        publishEvent(event, "order.checkIn");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void checkout(Long id, CheckOutDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!OrderStatus.已入住.name().equals(order.getStatus())) {
            throw new BusinessException(400, "订单当前状态不可退房");
        }
        order.setActualCheckOut(LocalDateTime.now());
        order.setStatus(OrderStatus.已完成.name());
        ordersMapper.updateById(order);
        roomFeignClient.changeStatus(order.getRoomId(), new RoomStatusChangeDTO(RoomStatus.待清洁中.name(), "办理退房"));

        int earnedPoints = order.getTotalAmount().intValue();
        OrderCheckoutEvent event = OrderCheckoutEvent.builder()
            .orderId(order.getId()).orderNo(order.getOrderNo())
            .eventType(OrderEventType.CHECK_OUT).status(OrderStatus.已完成.name())
            .customerId(order.getCustomerId()).customerName(order.getCustomerName())
            .roomId(order.getRoomId()).roomNumber(order.getRoomNumber())
            .actualCheckOut(order.getActualCheckOut()).totalAmount(order.getTotalAmount())
            .earnedPoints(earnedPoints > 0 ? earnedPoints : 0)
            .timestamp(LocalDateTime.now()).build();
        publishEvent(event, "order.checkout");
    }

    @Override
    @Transactional
    public Map<String, Object> extend(Long id, ExtendDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!OrderStatus.已入住.name().equals(order.getStatus())) {
            throw new BusinessException(400, "仅已入住订单可续住");
        }
        LocalDate newCheckOut = order.getCheckOutDate().plusDays(dto.getExtendDays());
        BigDecimal additionalAmount = order.getRoomPrice().multiply(BigDecimal.valueOf(dto.getExtendDays()));
        BigDecimal newTotal = order.getTotalAmount().add(additionalAmount);

        order.setCheckOutDate(newCheckOut);
        order.setNights(order.getNights() + dto.getExtendDays());
        order.setTotalAmount(newTotal);
        ordersMapper.updateById(order);

        OrderExtendedEvent event = OrderExtendedEvent.builder()
            .orderId(order.getId()).orderNo(order.getOrderNo())
            .eventType(OrderEventType.EXTENDED).status(OrderStatus.已入住.name())
            .customerId(order.getCustomerId()).customerName(order.getCustomerName())
            .roomId(order.getRoomId()).roomNumber(order.getRoomNumber())
            .newCheckOutDate(newCheckOut).extendDays(dto.getExtendDays()).additionalAmount(additionalAmount)
            .timestamp(LocalDateTime.now()).build();
        publishEvent(event, "order.extended");

        return Map.of("newCheckOutDate", newCheckOut.toString(),
                "additionalAmount", additionalAmount,
                "newTotalAmount", newTotal);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Map<String, Object> changeRoom(Long id, ChangeRoomDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!OrderStatus.已入住.name().equals(order.getStatus())) {
            throw new BusinessException(400, "仅已入住订单可换房");
        }
        if (dto.getNewRoomId().equals(order.getRoomId())) {
            throw new BusinessException(400, "新旧房间相同");
        }
        // ==================== 加分布式锁（防止并发换房）====================
        // 锁粒度：新房ID（最关键资源）
        String lockKey = "lock:room:" + dto.getNewRoomId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 立即获取锁，30秒自动释放
            boolean locked = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(400, "换房操作冲突，请稍后重试");
            }

            // ==================== 锁内执行业务 ====================
            // 4. 查询新旧房间信息
            Result<RoomVO> oldRoomResult = roomFeignClient.detail(order.getRoomId());
            Result<RoomVO> newRoomResult = roomFeignClient.detail(dto.getNewRoomId());

            RoomVO oldRoom = oldRoomResult.getData();
            RoomVO newRoom = newRoomResult.getData();

            if (oldRoom == null || newRoom == null) {
                throw new BusinessException(404, "房间不存在");
            }

            // 5. 校验新房必须是【空闲中】
            if (!RoomStatus.空闲中.name().equals(newRoom.getStatus())) {
                throw new BusinessException(400, "目标房间已被占用");
            }

            // ==================== 关键：状态修改顺序必须正确 ====================
            // 6. 先占用新房（空闲中 → 预订中 → 入住中，按合法跳转分两步）
            roomFeignClient.changeStatus(
                    dto.getNewRoomId(),
                    new RoomStatusChangeDTO(RoomStatus.预订中.name(), "换房预定")
            );
            roomFeignClient.changeStatus(
                    dto.getNewRoomId(),
                    new RoomStatusChangeDTO(RoomStatus.入住中.name(), "换房入住")
            );

            // 7. 再释放旧房间（入住中 → 待清洁中）
            roomFeignClient.changeStatus(
                    order.getRoomId(),
                    new RoomStatusChangeDTO(RoomStatus.待清洁中.name(), "换房腾出")
            );

            // 8. 更新订单信息（只更新房间相关字段，避免覆盖订单id和orderNo）
            String oldRoomNumber = order.getRoomNumber();
            order.setRoomId(dto.getNewRoomId());
            order.setRoomNumber(newRoom.getRoomNumber());
            order.setRoomTypeName(newRoom.getRoomTypeName());
            ordersMapper.updateById(order);

            // 9. 差价计算（你可以自己补）
            BigDecimal priceDiff = BigDecimal.ZERO;

            OrderRoomChangedEvent event = OrderRoomChangedEvent.builder()
                .orderId(order.getId()).orderNo(order.getOrderNo())
                .eventType(OrderEventType.ROOM_CHANGED).status(OrderStatus.已入住.name())
                .customerId(order.getCustomerId()).customerName(order.getCustomerName())
                .roomId(dto.getNewRoomId()).roomNumber(newRoom.getRoomNumber())
                .oldRoomId(order.getRoomId()).oldRoomNumber(oldRoomNumber)
                .newRoomId(dto.getNewRoomId()).newRoomNumber(newRoom.getRoomNumber())
                .priceDiff(priceDiff)
                .timestamp(LocalDateTime.now()).build();
            publishEvent(event, "order.roomChanged");

            return Map.of(
                    "oldRoomNumber", oldRoomNumber,
                    "newRoomNumber", order.getRoomNumber(),
                    "priceDiff", priceDiff
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "换房失败");
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public Map<String, Object> pay(Long id, PayDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        String paymentNo = SnowflakeIdUtil.generatePaymentNo();

        Payment payment = new Payment();
        payment.setPaymentNo(paymentNo);
        payment.setOrderId(id);
        payment.setOrderNo(order.getOrderNo());
        payment.setAmount(dto.getAmount());
        payment.setMethod(dto.getMethod());
        payment.setStatus("成功");
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        BigDecimal newPaid = order.getPaidAmount().add(dto.getAmount());
        order.setPaidAmount(newPaid);
        if (newPaid.compareTo(order.getTotalAmount()) >= 0) {
            order.setStatus(OrderStatus.已支付.name());
        }
        ordersMapper.updateById(order);

        BigDecimal remain = order.getTotalAmount().subtract(newPaid);

        OrderPaidEvent event = OrderPaidEvent.builder()
            .orderId(order.getId()).orderNo(order.getOrderNo())
            .eventType(OrderEventType.PAID).status(order.getStatus())
            .customerId(order.getCustomerId()).customerName(order.getCustomerName())
            .roomId(order.getRoomId()).roomNumber(order.getRoomNumber())
            .paidAmount(dto.getAmount()).paymentMethod(dto.getMethod()).paymentNo(paymentNo)
            .timestamp(LocalDateTime.now()).build();
        publishEvent(event, "order.paid");

        return Map.of("paymentNo", paymentNo,
                "paidAmount", newPaid,
                "remainAmount", remain.compareTo(BigDecimal.ZERO) > 0 ? remain : BigDecimal.ZERO);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Map<String, Object> refund(Long id, RefundDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (dto.getAmount().compareTo(order.getPaidAmount()) > 0) {
            throw new BusinessException(400, "退款金额超过已付金额");
        }
        Payment payment = paymentMapper.selectOne(new QueryWrapper<Payment>().eq("order_id", id));
        RefundRecordVO refundRecordVO = RefundRecordVO.builder()
                .orderId(SnowflakeIdUtil.nextId())
                .orderNo(order.getOrderNo())
                .paymentNo(payment.getPaymentNo())
                .refundAmount(dto.getAmount())
                .reason(dto.getReason())
                .status("成功")
                .operatorName("系统")
                .createTime(LocalDateTime.now()).build();
        financeRefundFeignClient.add(refundRecordVO);

        order.setPaidAmount(order.getPaidAmount().subtract(dto.getAmount()));
        order.setStatus(OrderStatus.已退款.name());
        ordersMapper.updateById(order);

        OrderRefundEvent event = OrderRefundEvent.builder()
            .orderId(order.getId()).orderNo(order.getOrderNo())
            .eventType(OrderEventType.REFUNDED).status(OrderStatus.已退款.name())
            .customerId(order.getCustomerId()).customerName(order.getCustomerName())
            .roomId(order.getRoomId()).roomNumber(order.getRoomNumber())
            .refundAmount(dto.getAmount()).refundReason(dto.getReason())
            .timestamp(LocalDateTime.now()).build();
        publishEvent(event, "order.refund");

        return Map.of("refundAmount", dto.getAmount());
    }

    @Override
    public List<PaymentVO> getPayments(Long id) {
        List<Payment> payments = paymentMapper.selectList(new QueryWrapper<Payment>().eq("order_id", id));
        if(payments.isEmpty()){
            return List.of();
        }
        List<PaymentVO> paymentsResult = new ArrayList<>();
        BeanUtil.copyProperties(payments, paymentsResult);
        return paymentsResult;
    }

    @Override
    @Transactional
    public Map<String, Object> addExtra(Long id, ExtraDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!OrderStatus.已入住.name().equals(order.getStatus())) {
            throw new BusinessException(400, "仅已入住订单可添加消费");
        }

        OrderExtra extra = new OrderExtra();
        extra.setOrderId(id);
        extra.setItemName(dto.getItemName());
        extra.setAmount(dto.getAmount());
        extra.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        extra.setOperatorId(UserContext.getUserId());
        orderExtraMapper.insert(extra);

        BigDecimal extraSubtotal = dto.getAmount().multiply(
                BigDecimal.valueOf(dto.getQuantity() != null ? dto.getQuantity() : 1));
        order.setExtraTotal(order.getExtraTotal().add(extraSubtotal));
        order.setTotalAmount(order.getTotalAmount().add(extraSubtotal));
        ordersMapper.updateById(order);

        return Map.of("extraId", extra.getId());
    }

    @Override
    public List<ExtraVO> getExtras(Long id) {
        List<OrderExtra> extraList = orderExtraMapper.selectList(new QueryWrapper<OrderExtra>().eq("order_id", id));
        List<ExtraVO> extraVOList = new ArrayList<>();
        for(OrderExtra extra : extraList){
            ExtraVO extraVO = ExtraVO.builder()
                    .id(extra.getId())
                    .itemName(extra.getItemName())
                    .amount(extra.getAmount())
                    .quantity(extra.getQuantity())
                    .subtotal(extra.getAmount().multiply(BigDecimal.valueOf(extra.getQuantity())))
                    .operatorName(UserContext.getUsername())
                    .createTime(extra.getCreateTime()).build();
            extraVOList.add(extraVO);
        }
        return extraVOList;
    }

    @Override
    @Transactional
    public void deleteExtra(Long id, Long extraId) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!OrderStatus.已入住.name().equals(order.getStatus())) {
            throw new BusinessException(400, "仅已入住订单可删除消费");
        }

        OrderExtra extra = orderExtraMapper.selectById(extraId);
        if (extra == null || !extra.getOrderId().equals(id)) {
            throw new BusinessException(404, "消费项不存在");
        }
        BigDecimal subtotal = extra.getAmount().multiply(BigDecimal.valueOf(extra.getQuantity()));
        order.setExtraTotal(order.getExtraTotal().subtract(subtotal));
        order.setTotalAmount(order.getTotalAmount().subtract(subtotal));
        ordersMapper.updateById(order);

        orderExtraMapper.deleteById(extraId);
    }

    @Override
    public PageResult<OrderVO> myList(Integer page, Integer size, String status) {
        // 1. 创建 MP 分页对象
        IPage<Orders> ordersPage = new Page<>(page, size);

        // 2. 调用 MyBatis-Plus 分页查询
        QueryWrapper<Orders> wrapper = new QueryWrapper<Orders>()
                .eq("customer_id", UserContext.getUserId());
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        ordersPage = ordersMapper.selectPage(ordersPage, wrapper);

        // 3. 转 VO
        List<OrderVO> orderVOList = BeanUtil.copyToList(ordersPage.getRecords(), OrderVO.class);

        // 4. 返回分页结果（正确的 total、page、size）
        return PageResult.of(ordersPage.getTotal(), page, size, orderVOList);
    }

    @Override
    public OrderVO myGetById(Long id) {
        OrderVO orderVO = OrderVO.builder().build();
        // 获取到订单信息
        Orders order = ordersMapper.selectById(id);
        // 复制属性
        BeanUtil.copyProperties(order, orderVO);

        // 获取订单的消费项

        List<OrderExtra> extraList = orderExtraMapper.selectList(new QueryWrapper<OrderExtra>().eq("order_id", order.getId()));

        orderVO.setExtras(BeanUtil.copyToList(extraList, ExtraVO.class));
        return orderVO;
    }

    @Override
    public List<PaymentVO> getPaymentsByTime(String startTime, String endTime) {
        List<Payment> paymentList = paymentMapper.selectList(new QueryWrapper<Payment>().ge("paid_at", startTime).le("paid_at", endTime));
        List<PaymentVO> paymentVOList = new ArrayList<>();
        for(Payment payment : paymentList){
            PaymentVO paymentVO = PaymentVO.builder()
                    .id(payment.getId())
                    .paymentNo(payment.getPaymentNo())
                    .amount(payment.getAmount())
                    .method(payment.getMethod())
                    .methodName(PaymentMethod.valueOf(payment.getMethod()).getLabel())
                    .status(payment.getStatus())
                    .paidAt(payment.getPaidAt())
                    .build();
            paymentVOList.add(paymentVO);
        }
        return paymentVOList;
    }

    @Override
    public List<OrderVO> listByTime(String startDate, String endDate) {
        List<Orders> orderList = ordersMapper.selectList(
                new QueryWrapper<Orders>()
                        .ge("DATE(create_time)", startDate)  // 截取日期比较，字符串直接匹配
                        .le("DATE(create_time)", endDate)
        );
        return BeanUtil.copyToList(orderList, OrderVO.class);
    }

    @Override
    public Map<String, Object> getAvailableRooms(Long roomTypeId, String checkIn, String checkOut) {
        // 1. 获取该房型所有房间
        Result<PageResult<RoomVO>> listResult = roomFeignClient.list(1, 200, roomTypeId, null, null, null);
        PageResult<RoomVO> pageResult = listResult.getData();
        if (pageResult == null || pageResult.getRecords() == null || pageResult.getRecords().isEmpty()) {
            return Map.of("availableCount", 0, "roomIds", List.of());
        }
        List<RoomVO> allRooms = pageResult.getRecords();

        // 2. 查询与指定日期范围重叠的活跃订单
        List<Long> allRoomIds = allRooms.stream().map(RoomVO::getId).toList();
        List<Orders> overlappingOrders = ordersMapper.selectList(new QueryWrapper<Orders>()
                .in("room_id", allRoomIds)
                .notIn("status", "已取消", "已完成", "已退款")
                .lt("check_in_date", checkOut)
                .gt("check_out_date", checkIn));

        // 3. 收集被占用的房间ID
        Set<Long> occupiedRoomIds = overlappingOrders.stream()
                .map(Orders::getRoomId)
                .collect(java.util.stream.Collectors.toSet());

        // 4. 筛选可用房间
        List<Long> availableRoomIds = allRooms.stream()
                .map(RoomVO::getId)
                .filter(id -> !occupiedRoomIds.contains(id))
                .toList();

        return Map.of("availableCount", availableRoomIds.size(), "roomIds", availableRoomIds);
    }

    @Override
    public int autoCancelExpired(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        List<Orders> expiredOrders = ordersMapper.selectList(new QueryWrapper<Orders>()
                .eq("status", "待支付")
                .lt("create_time", cutoff));
        int count = 0;
        for (Orders order : expiredOrders) {
            try {
                order.setStatus("已取消");
                ordersMapper.updateById(order);
                roomFeignClient.changeStatus(order.getRoomId(),
                        new RoomStatusChangeDTO(RoomStatus.空闲中.name(), "超时自动取消"));
                count++;
                log.info("自动取消超时订单: orderNo={}, roomId={}", order.getOrderNo(), order.getRoomId());
                OrderCancelledEvent event = OrderCancelledEvent.builder()
                    .orderId(order.getId()).orderNo(order.getOrderNo())
                    .eventType(OrderEventType.AUTO_CANCELLED).status(OrderStatus.已取消.name())
                    .customerId(order.getCustomerId()).customerName(order.getCustomerName())
                    .roomId(order.getRoomId()).roomNumber(order.getRoomNumber())
                    .cancelReason("超时" + minutes + "分钟自动取消")
                    .timestamp(LocalDateTime.now()).build();
                publishEvent(event, "order.cancelled");
            } catch (Exception e) {
                log.warn("自动取消订单失败: orderNo={}", order.getOrderNo(), e);
            }
        }
        return count;
    }

    private void publishEvent(OrderEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend("hotel.order.events", routingKey, event);
        } catch (Exception e) {
            log.error("发布订单事件失败: orderNo={}, routingKey={}", event.getOrderNo(), routingKey, e);
        }
    }

    private void unlockRooms(List<Long> roomIds) {
        for (Long roomId : roomIds) {
            RLock lock = redissonClient.getLock("lock:room:" + roomId);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
