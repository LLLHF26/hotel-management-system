package com.lhf.hotel.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.lhf.hotel.order.model.entity.Product;
import com.lhf.hotel.common.util.UserContext;
import com.lhf.hotel.common.model.vo.ExtraVO;
import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.common.model.vo.PaymentVO;
import com.lhf.hotel.order.model.vo.HotRoomTypeCountVO;
import com.lhf.hotel.order.model.vo.ProductOrderVO;
import com.lhf.hotel.order.service.OrderService;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import com.lhf.hotel.room.model.vo.RoomVO;
import com.lhf.hotel.user.model.vo.CustomerVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;
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

    // feign调用system-service（读取积分抵扣比例）
    @Resource
    private com.lhf.hotel.order.feign.systemService.SystemSettingFeignClient settingFeignClient;

    @Resource
    private com.lhf.hotel.order.model.mapper.ProductMapper productMapper;

    /** 积分抵扣默认比例：1 积分可抵扣的金额（元） */
    private static final BigDecimal DEFAULT_POINTS_RATIO = new BigDecimal("0.1");

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(OrderCreateDTO dto, String idempotencyKey) {
        // 0. 幂等性校验
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String idemRedisKey = "idempotency:order:create:" + idempotencyKey;
            Boolean claimed = stringRedisTemplate.opsForValue()
                    .setIfAbsent(idemRedisKey, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(claimed)) {
                throw new BusinessException(409, "重复请求，幂等键已被使用");
            }
        }

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
                boolean isLock = lock.tryLock(5, 30, TimeUnit.SECONDS);
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
        // 注意：统一使用登录用户（从 token 解析，Java 侧 Long 精确），不要信任前端传来的 customerId。
        // 前端 Storage 里的 customerId 是 19 位雪花 ID，经 JSON.parse 会精度丢失变成另一个数，
        // 直接用会导致 user-service 查不到会员（404）。token 由后端签发/解析，不存在精度问题。
        Long customerId = UserContext.getUserId();
        if (customerId == null) {
            unlockRooms(lockedRoomIds);
            throw new BusinessException(400, "会员不能为空，请先登录");
        }

        Result<CustomerVO> customerVOResult = userFeignClient.detail(customerId);
        CustomerVO customer = customerVOResult.getData();
        if (customer == null) {
            unlockRooms(lockedRoomIds);
            throw new BusinessException(404, "会员不存在");
        }
        Result<RoomTypeVO> roomTypeResult = roomTypeFeignClient.detail(dto.getRoomTypeId());
        RoomTypeVO roomType = roomTypeResult.getData();
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

        // 6. 为每间房创建订单（先写入订单，再修改房态）
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
            // 先写入订单（本地事务保护）
            ordersMapper.insert(order);

            // 再调用远程服务修改房态（带重试）
            try {
                RoomStatusChangeDTO changeDTO = new RoomStatusChangeDTO(RoomStatus.预订中.name(), "订单创建");
                retryOnFeignException(2, 200, "修改房间状态 roomId=" + roomId,
                        () -> roomFeignClient.changeStatus(roomId, changeDTO));
            } catch (Exception e) {
                // 补偿：房态修改失败，将订单置为已取消
                log.warn("修改房间状态失败，订单 {} 将被取消: {}", orderNo, e.getMessage());
                order.setStatus(OrderStatus.已取消.name());
                ordersMapper.updateById(order);
                unlockRooms(lockedRoomIds);
                throw new BusinessException(500, "预订过程中房间状态更新失败，订单已取消");
            }

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
    @Transactional(rollbackFor = Exception.class)
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
        retryOnFeignException(2, 200, "取消订单-修改房间状态 roomId=" + order.getRoomId(),
                () -> roomFeignClient.changeStatus(order.getRoomId(),
                        new RoomStatusChangeDTO(RoomStatus.空闲中.name(), "订单取消")));

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
    @Transactional(rollbackFor = Exception.class)
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
        retryOnFeignException(2, 200, "办理入住-修改房间状态 roomId=" + order.getRoomId(),
                () -> roomFeignClient.changeStatus(order.getRoomId(),
                        new RoomStatusChangeDTO(RoomStatus.入住中.name(), "办理入住")));

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
    @Transactional(rollbackFor = Exception.class)
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
        retryOnFeignException(2, 200, "办理退房-修改房间状态 roomId=" + order.getRoomId(),
                () -> roomFeignClient.changeStatus(order.getRoomId(),
                        new RoomStatusChangeDTO(RoomStatus.待清洁中.name(), "办理退房")));

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
    @Transactional(rollbackFor = Exception.class)
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

        // 解析并校验可选的换房时间段
        LocalDate effectiveStart = order.getCheckInDate();
        LocalDate effectiveEnd = order.getCheckOutDate();
        boolean hasCustomRange = false;
        if (dto.getStartDate() != null && !dto.getStartDate().isBlank()) {
            LocalDate reqStart = LocalDate.parse(dto.getStartDate());
            if (reqStart.isBefore(order.getCheckInDate()) || reqStart.isAfter(order.getCheckOutDate())) {
                throw new BusinessException(400,
                    "换房起始日期必须在订单入住日期(" + order.getCheckInDate()
                    + ")和退房日期(" + order.getCheckOutDate() + ")之间");
            }
            effectiveStart = reqStart;
            hasCustomRange = true;
        }
        if (dto.getEndDate() != null && !dto.getEndDate().isBlank()) {
            LocalDate reqEnd = LocalDate.parse(dto.getEndDate());
            if (reqEnd.isBefore(effectiveStart) || reqEnd.isAfter(order.getCheckOutDate())) {
                throw new BusinessException(400, "换房结束日期必须在换房起始日期和订单退房日期之间");
            }
            effectiveEnd = reqEnd;
            hasCustomRange = true;
        }

        // ==================== 加分布式锁（防止并发换房）====================
        // 锁粒度：新房ID（最关键资源）
        String lockKey = "lock:room:" + dto.getNewRoomId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 立即获取锁，30秒自动释放
            boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
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
            retryOnFeignException(2, 200, "换房-预订新房 roomId=" + dto.getNewRoomId(),
                    () -> roomFeignClient.changeStatus(
                            dto.getNewRoomId(),
                            new RoomStatusChangeDTO(RoomStatus.预订中.name(), "换房预定"))
            );
            retryOnFeignException(2, 200, "换房-入住新房 roomId=" + dto.getNewRoomId(),
                    () -> roomFeignClient.changeStatus(
                            dto.getNewRoomId(),
                            new RoomStatusChangeDTO(RoomStatus.入住中.name(), "换房入住"))
            );

            // 7. 再释放旧房间（入住中 → 待清洁中）
            retryOnFeignException(2, 200, "换房-腾出旧房 roomId=" + order.getRoomId(),
                    () -> roomFeignClient.changeStatus(
                            order.getRoomId(),
                            new RoomStatusChangeDTO(RoomStatus.待清洁中.name(), "换房腾出"))
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

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("oldRoomNumber", oldRoomNumber);
            result.put("newRoomNumber", order.getRoomNumber());
            result.put("priceDiff", priceDiff);
            if (hasCustomRange) {
                result.put("changeDateRange", effectiveStart + " ~ " + effectiveEnd);
            }
            return result;

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
        log.info("【支付请求】orderId={}, amount={}, method={}", id, dto.getAmount(), dto.getMethod());
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            // 尝试按 orderNo 查询
            order = ordersMapper.selectOne(new QueryWrapper<Orders>().eq("order_no", String.valueOf(id)));
            if (order == null) {
                log.warn("【支付失败】订单不存在，id={}, 已尝试按 orderNo 查询", id);
                throw new BusinessException(404, "订单不存在（id=" + id + "）");
            }
            log.info("【支付】按 orderNo={} 找到订单，实际id={}", id, order.getId());
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
    @Transactional(rollbackFor = Exception.class)
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
        retryOnFeignException(2, 200, "添加退款记录 orderId=" + id,
                () -> financeRefundFeignClient.add(refundRecordVO));

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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> productOrder(Long id, ProductOrderDTO dto) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        Long customerId = UserContext.getUserId();
        if (customerId == null || !customerId.equals(order.getCustomerId())) {
            throw new BusinessException(403, "无权操作该订单");
        }
        if (!"已入住".equals(order.getStatus()) && !"已支付".equals(order.getStatus())) {
            throw new BusinessException(400, "仅已入住 / 已支付订单可下单客房商品");
        }

        // 1. 读取积分抵扣比例（来自 system_setting）
        BigDecimal ratio = DEFAULT_POINTS_RATIO;
        boolean enabled = true;
        try {
            var settingRes = settingFeignClient.listSettings();
            if (settingRes != null && settingRes.getData() != null) {
                for (Map<String, Object> item : settingRes.getData()) {
                    String k = String.valueOf(item.get("key"));
                    String v = item.get("value") == null ? null : String.valueOf(item.get("value"));
                    if ("points_discount_ratio".equals(k) && v != null && !v.isBlank()) {
                        ratio = new BigDecimal(v);
                    } else if ("points_discount_enabled".equals(k) && v != null) {
                        enabled = "true".equalsIgnoreCase(v) || "1".equals(v);
                    }
                }
            }
        } catch (Exception ignored) { /* 使用默认比例 */ }

        // 2. 计算商品小计
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderExtra> extras = new ArrayList<>();
        for (ProductOrderDTO.Item item : dto.getItems()) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || !"上架".equals(product.getStatus())) {
                throw new BusinessException(400, "商品[" + item.getProductId() + "]不可购买");
            }
            int qty = item.getQuantity() == null ? 1 : item.getQuantity();
            OrderExtra extra = new OrderExtra();
            extra.setOrderId(id);
            extra.setItemName(product.getName());
            extra.setAmount(product.getPrice());
            extra.setQuantity(qty);
            extra.setOperatorId(customerId);
            extras.add(extra);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        // 3. 积分抵扣
        int pointsUsed = dto.getPointsUsed() == null ? 0 : dto.getPointsUsed();
        BigDecimal discount = BigDecimal.ZERO;
        int effectivePoints = 0;
        if (enabled && pointsUsed > 0) {
            // 客户积分余额校验
            Result<CustomerVO> custRes = userFeignClient.detail(customerId);
            int balance = (custRes != null && custRes.getData() != null && custRes.getData().getPoints() != null)
                    ? custRes.getData().getPoints() : 0;
            if (pointsUsed > balance) {
                throw new BusinessException(400, "可用积分不足");
            }
            discount = ratio.multiply(BigDecimal.valueOf(pointsUsed));
            // 抵扣金额不超过小计
            if (discount.compareTo(subtotal) > 0) {
                discount = subtotal;
                effectivePoints = discount.divide(ratio, 0, java.math.RoundingMode.DOWN).intValue();
            } else {
                effectivePoints = pointsUsed;
            }
        }

        // 4. 落库：消费项 + 订单金额累加
        for (OrderExtra extra : extras) {
            orderExtraMapper.insert(extra);
        }
        order.setExtraTotal(order.getExtraTotal().add(subtotal));
        order.setTotalAmount(order.getTotalAmount().add(subtotal));
        ordersMapper.updateById(order);

        // 5. 扣减积分
        if (effectivePoints > 0) {
            userFeignClient.addPoints(customerId, -effectivePoints, "客房商品积分抵扣");
        }

        BigDecimal payable = subtotal.subtract(discount);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subtotal", subtotal);
        result.put("pointsUsed", effectivePoints);
        result.put("discount", discount);
        result.put("payable", payable);
        return result;
    }

    @Override
    public PageResult<ProductOrderVO> recentProductOrders(int page, int size) {
        IPage<OrderExtra> p = new Page<>(page, size);
        p = orderExtraMapper.selectPage(p,
                Wrappers.<OrderExtra>lambdaQuery().orderByDesc(OrderExtra::getCreateTime));
        List<OrderExtra> extras = p.getRecords();
        List<ProductOrderVO> vos = new ArrayList<>();
        if (!extras.isEmpty()) {
            Set<Long> orderIds = extras.stream()
                    .map(OrderExtra::getOrderId)
                    .collect(Collectors.toSet());
            List<Orders> ordersList = ordersMapper.selectBatchIds(orderIds);
            Map<Long, Orders> orderMap = ordersList.stream()
                    .collect(Collectors.toMap(Orders::getId, o -> o, (a, b) -> a));
            for (OrderExtra e : extras) {
                ProductOrderVO vo = ProductOrderVO.builder()
                        .id(e.getId())
                        .orderId(e.getOrderId())
                        .itemName(e.getItemName())
                        .amount(e.getAmount())
                        .quantity(e.getQuantity())
                        .operatorId(e.getOperatorId())
                        .createTime(e.getCreateTime())
                        .build();
                Orders o = orderMap.get(e.getOrderId());
                if (o != null) {
                    vo.setOrderNo(o.getOrderNo());
                    vo.setRoomNumber(o.getRoomNumber());
                    vo.setCustomerName(o.getCustomerName());
                }
                vos.add(vo);
            }
        }
        return PageResult.of(p.getTotal(), page, size, vos);
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
        log.info("【查询订单详情】id={}", id);
        // 获取到订单信息
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            // 尝试按 orderNo 查询（兼容前端可能传了 orderNo 的情况）
            order = ordersMapper.selectOne(new QueryWrapper<Orders>().eq("order_no", String.valueOf(id)));
            if (order == null) {
                log.warn("【订单不存在】id={}, 已尝试按 orderNo 查询均无结果", id);
                return OrderVO.builder().build();
            }
            log.info("【按 orderNo 找到订单】orderNo={}, 实际id={}", id, order.getId());
        }
        OrderVO orderVO = OrderVO.builder().build();
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
                retryOnFeignException(1, 200, "自动取消-修改房间状态 roomId=" + order.getRoomId(),
                        () -> roomFeignClient.changeStatus(order.getRoomId(),
                                new RoomStatusChangeDTO(RoomStatus.空闲中.name(), "超时自动取消")));
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
            retryOnFeignException(2, 200,
                    "发布订单事件 orderNo=" + event.getOrderNo() + " routingKey=" + routingKey,
                    () -> rabbitTemplate.convertAndSend("hotel.order.events", routingKey, event));
        } catch (Exception e) {
            log.error("[严重] 订单事件发送失败（重试耗尽），数据可能丢失！orderNo={}, routingKey={}, eventType={}",
                    event.getOrderNo(), routingKey, event.getEventType(), e);
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

    /**
     * 简单重试工具：对 Feign 调用等可能因网络抖动失败的操作进行重试
     */
    private void retryOnFeignException(int maxRetries, long baseSleepMs,
                                       String description, Runnable action) {
        for (int i = 0; i <= maxRetries; i++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                log.warn("{} 失败 (第{}次): {}", description, i + 1, e.getMessage());
                if (i >= maxRetries) {
                    throw e;
                }
                try {
                    long sleep = baseSleepMs * (1L << i);
                    Thread.sleep(sleep);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(500, description + " 被中断");
                }
            }
        }
    }

    @Override
    public List<HotRoomTypeCountVO> getHotRoomTypeCounts(int topN, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> rows = ordersMapper.selectHotRoomTypeCounts(startDate, topN);
        return rows.stream()
                .map(row -> new HotRoomTypeCountVO(
                        (String) row.get("roomTypeName"),
                        ((Number) row.get("orderCount")).longValue()))
                .toList();
    }

    @Override
    public List<OrderVO> getRoomSchedule(Long roomId, String startDate, String endDate) {
        // 默认查询未来 30 天
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now();
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusDays(30);
        // 查询该房间在日期范围内非取消的订单（含待支付/已支付/已入住）
        List<Orders> orderList = ordersMapper.selectList(new QueryWrapper<Orders>()
                .eq("room_id", roomId)
                .ne("status", "已取消")
                .ge("check_in_date", start.toString())
                .le("check_in_date", end.toString())
                .orderByAsc("check_in_date")
        );
        return BeanUtil.copyToList(orderList, OrderVO.class);
    }
}
