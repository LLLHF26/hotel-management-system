package com.lhf.hotel.finance.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.sql.Order;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.dto.PageQuery;
import com.lhf.hotel.common.enums.PaymentMethod;
import com.lhf.hotel.common.model.vo.RefundRecordVO;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.StrUtil;
import com.lhf.hotel.finance.feign.orderService.OrderFeignClient;
import com.lhf.hotel.finance.feign.orderService.PaymentFeignClient;
import com.lhf.hotel.finance.feign.roomService.RoomFeignClient;
import com.lhf.hotel.finance.feign.roomService.RoomTypeFeignClient;
import com.lhf.hotel.finance.mapper.DailyRevenueMapper;
import com.lhf.hotel.finance.mapper.RefundRecordMapper;
import com.lhf.hotel.finance.model.entity.DailyRevenue;
import com.lhf.hotel.finance.model.entity.RefundRecord;
import com.lhf.hotel.finance.model.vo.*;
import com.lhf.hotel.finance.service.FinanceService;

import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.common.model.vo.PaymentVO;
import com.lhf.hotel.common.util.UserContext;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import com.lhf.hotel.room.model.vo.RoomVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Wrapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceServiceImpl implements FinanceService {

    private final DailyRevenueMapper dailyRevenueMapper;
    private final RefundRecordMapper refundRecordMapper;

    @Resource
    private PaymentFeignClient paymentFeignClient;
    @Resource
    private OrderFeignClient orderFeignClient;
    @Resource
    private RoomTypeFeignClient roomTypeFeignClient;
    @Resource
    private RoomFeignClient roomFeignClient;

    @Override
    public RevenueSummaryVO revenueSummary() {
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);
        // 获取今年第一天
        LocalDate firstDayOfYear = LocalDate.of(now.getYear(), 1, 1);
        // 获取明年第一天（作为结束边界）
        LocalDate firstDayOfNextYear = firstDayOfYear.plusYears(1);

        List<DailyRevenue> thisYearRevenue = dailyRevenueMapper.selectList(
                new QueryWrapper<DailyRevenue>()
                        .ge("date", firstDayOfYear)    // >= 今年1月1日
                        .lt("date", firstDayOfNextYear) // < 明年1月1日
        );
        if(CollectionUtils.isEmpty(thisYearRevenue)){
            return computeSummaryFromOrders(now, firstDayOfYear);
        }
        // 这里认为数据库的数据全是按天统计的，所以这里直接取第一个
        List<DailyRevenue> todayRevenue = thisYearRevenue.stream().filter(revenue -> revenue.getDate().equals(now)).toList();
        List<DailyRevenue> yesterdayRevenue = thisYearRevenue.stream().filter(revenue -> revenue.getDate().equals(yesterday)).toList();
        List<DailyRevenue> thisMonthRevenue = thisYearRevenue.stream().filter(revenue -> revenue.getDate().getMonth().equals(now.getMonth())).toList();
        TodaySummary today = null;
        if(!todayRevenue.isEmpty()){
            DailyRevenue tr = todayRevenue.getFirst();
            today = buildTodaySummary(tr, now);
        }
        TodaySummary yesterdaySummary = null;
        if(!yesterdayRevenue.isEmpty()){
            DailyRevenue yr = yesterdayRevenue.getFirst();
            yesterdaySummary = buildTodaySummary(yr, yesterday);
        }

        DailyRevenue message = new DailyRevenue();
        thisMonthRevenue.forEach(revenue -> {
            message.setRoomRevenue(message.getRoomRevenue()==null?revenue.getRoomRevenue():message.getRoomRevenue().add(revenue.getRoomRevenue()));
            message.setExtraRevenue(message.getExtraRevenue()==null?revenue.getExtraRevenue():message.getExtraRevenue().add(revenue.getExtraRevenue()));
            message.setTotalRevenue(message.getTotalRevenue()==null?revenue.getTotalRevenue():message.getTotalRevenue().add(revenue.getTotalRevenue()));
            message.setOrderCount(message.getOrderCount()==null?revenue.getOrderCount():message.getOrderCount() + revenue.getOrderCount());
        });
        MonthSummary thisMonth = MonthSummary.builder()
                .month(now.getMonth().toString())
                .roomRevenue(message.getRoomRevenue())
                .extraRevenue(message.getExtraRevenue())
                .totalRevenue(message.getTotalRevenue())
                .orderCount(message.getOrderCount())
                .avgDailyRevenue(
                        message.getTotalRevenue().divide(
                                BigDecimal.valueOf(thisMonthRevenue.size()),
                                2,
                                RoundingMode.HALF_UP
                        )
                )
                .build();
        DailyRevenue messageYear = new DailyRevenue();
        thisYearRevenue.forEach(revenue -> {
            messageYear.setRoomRevenue(messageYear.getRoomRevenue()==null?revenue.getRoomRevenue():messageYear.getRoomRevenue().add(revenue.getRoomRevenue()));
            messageYear.setExtraRevenue(messageYear.getExtraRevenue()==null?revenue.getExtraRevenue():messageYear.getExtraRevenue().add(revenue.getExtraRevenue()));
            messageYear.setTotalRevenue(messageYear.getTotalRevenue()==null?revenue.getTotalRevenue():messageYear.getTotalRevenue().add(revenue.getTotalRevenue()));
            messageYear.setOrderCount(messageYear.getOrderCount()==null?revenue.getOrderCount():messageYear.getOrderCount() + revenue.getOrderCount());
        });

        YearSummary thisYear = YearSummary.builder()
                .year(String.valueOf(now.getYear()))
                .roomRevenue(messageYear.getRoomRevenue())
                .extraRevenue(messageYear.getExtraRevenue())
                .totalRevenue(messageYear.getTotalRevenue())
                .orderCount(messageYear.getOrderCount())
                .build();

        return RevenueSummaryVO.builder()
                .today(today)
                .yesterday(yesterdaySummary)
                .thisMonth(thisMonth)
                .thisYear(thisYear)
                .build();
    }

    @Override
    public DailyTrendVO revenueDaily(String startDate, String endDate) {
        List<DailyRevenue> dailyRevenue = dailyRevenueMapper.selectList(new QueryWrapper<DailyRevenue>().ge("date", startDate).le("date", endDate));
        if (dailyRevenue == null) {
            dailyRevenue = new ArrayList<>();
        }
        if (dailyRevenue.isEmpty()) {
            return computeDailyFromOrders(startDate, endDate);
        }
        dailyRevenue.sort(Comparator.comparing(DailyRevenue::getDate));
        List<String> dates = dailyRevenue.stream().map(DailyRevenue::getDate).map(LocalDate::toString).toList();
        List<BigDecimal> roomRevenue = dailyRevenue.stream().map(DailyRevenue::getRoomRevenue).toList();
        List<BigDecimal> extraRevenue = dailyRevenue.stream().map(DailyRevenue::getExtraRevenue).toList();
        List<BigDecimal> totalRevenue = dailyRevenue.stream().map(DailyRevenue::getTotalRevenue).toList();
        List<Integer> orderCount = dailyRevenue.stream().map(DailyRevenue::getOrderCount).toList();
        return DailyTrendVO.builder()
                .dates(dates)
                .roomRevenue(roomRevenue)
                .extraRevenue(extraRevenue)
                .totalRevenue(totalRevenue)
                .orderCount(orderCount)
                .build();
    }

    @Override
    public List<MonthlyRevenueItemVO> revenueMonthly(Integer year) {
        if (year == null) {
            year = java.time.Year.now().getValue();
        }
        List<DailyRevenue> dailyRevenue = dailyRevenueMapper.selectList(new QueryWrapper<DailyRevenue>().eq("YEAR(date)", year));
        List<MonthlyRevenueItemVO> monthlyRevenueItemVOList = new ArrayList<>();
        monthlyRevenueItemVOList.add(MonthlyRevenueItemVO.builder().build());
        for (int i = 1; i <= 12; i++) {
            MonthlyRevenueItemVO monthlyRevenueItemVO = MonthlyRevenueItemVO.builder()
                    .month(String.valueOf(i))
                    .build();
            monthlyRevenueItemVOList.add(monthlyRevenueItemVO);
        }
        dailyRevenue.forEach(revenue -> {
            monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).setRoomRevenue(monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).getRoomRevenue().add(revenue.getRoomRevenue()));
            monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).setExtraRevenue(monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).getExtraRevenue().add(revenue.getExtraRevenue()));
            monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).setTotalRevenue(monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).getTotalRevenue().add(revenue.getTotalRevenue()));
            monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).setOrderCount(monthlyRevenueItemVOList.get(revenue.getDate().getMonth().getValue()).getOrderCount() + revenue.getOrderCount());
        });
        return monthlyRevenueItemVOList;
    }

    @Override
    public RevenueRangeVO revenueRange(String startDate, String endDate) {
        List<DailyRevenue> dailyRevenue = dailyRevenueMapper.selectList(new QueryWrapper<DailyRevenue>().ge("date", startDate).le("date", endDate));
        if (dailyRevenue == null) {
            dailyRevenue = new ArrayList<>();
        }
        RevenueRangeVO message = RevenueRangeVO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        dailyRevenue.forEach(revenue -> {
            message.setRoomRevenue(message.getRoomRevenue().add(revenue.getRoomRevenue()));
            message.setExtraRevenue(message.getExtraRevenue().add(revenue.getExtraRevenue()));
            message.setTotalRevenue(message.getTotalRevenue().add(revenue.getTotalRevenue()));
            message.setOrderCount(message.getOrderCount() + revenue.getOrderCount());
            message.setCheckInCount(message.getCheckInCount() + revenue.getCheckInCount());
            message.setCheckOutCount(message.getCheckOutCount() + revenue.getCheckOutCount());
            message.getPaymentBreakdown().put(PaymentMethod.CASH.name(), message.getPaymentBreakdown().get(PaymentMethod.CASH.name()).add(revenue.getCashAmount()));
            message.getPaymentBreakdown().put(PaymentMethod.ALIPAY.name(), message.getPaymentBreakdown().get(PaymentMethod.ALIPAY.name()).add(revenue.getAlipayAmount()));
            message.getPaymentBreakdown().put(PaymentMethod.WECHAT.name(), message.getPaymentBreakdown().get(PaymentMethod.WECHAT.name()).add(revenue.getWechatAmount()));
            message.getPaymentBreakdown().put(PaymentMethod.CARD.name(), message.getPaymentBreakdown().get(PaymentMethod.CARD.name()).add(revenue.getCardAmount()));
        });
        message.setAvgOccupancyRate(
                message.getCheckInCount() == 0 ? "0%" :
                        String.format("%.2f", message.getCheckInCount() * 100.0 / message.getOrderCount()) + "%"
        );
        return message;
    }

    @Override
    public PageResult<RevenueDetailVO> revenueDetail(Integer page, Integer size, String startDate, String endDate,
                                                     String paymentMethod, String orderNo, String roomNumber) {
        List<PaymentVO> paymentVOList;
        if (StrUtil.isNotBlank(orderNo)) {
            paymentVOList = Optional.ofNullable(paymentFeignClient.getPayments(Long.valueOf(orderNo)))
                    .map(Result::getData).orElse(Collections.emptyList());
        } else if (StrUtil.isNotBlank(startDate) && StrUtil.isNotBlank(endDate)) {
            paymentVOList = Optional.ofNullable(paymentFeignClient.getPaymentsByTime(startDate, endDate))
                    .map(Result::getData).orElse(Collections.emptyList());
        } else {
            paymentVOList = Collections.emptyList();
        }

        if (StrUtil.isNotBlank(startDate)) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            paymentVOList = paymentVOList.stream()
                    .filter(p -> p.getPaidAt() != null && p.getPaidAt().isAfter(start))
                    .toList();
        }
        if (StrUtil.isNotBlank(endDate)) {
            LocalDateTime end = LocalDateTime.parse(endDate);
            paymentVOList = paymentVOList.stream()
                    .filter(p -> p.getPaidAt() != null && p.getPaidAt().isBefore(end))
                    .toList();
        }
        if (StrUtil.isNotBlank(paymentMethod)) {
            paymentVOList = paymentVOList.stream()
                    .filter(p -> paymentMethod.equals(p.getMethod()))
                    .toList();
        }
        if (StrUtil.isNotBlank(orderNo)) {
            paymentVOList = paymentVOList.stream()
                    .filter(p -> orderNo.equals(p.getPaymentNo()))
                    .toList();
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, paymentVOList.size());
        PageResult<RevenueDetailVO> message = PageResult.of(paymentVOList.size(), page, size, Collections.emptyList());
        for (int i = start; i < end; i++) {
            RevenueDetailVO vo = RevenueDetailVO.builder().build();
            BeanUtil.copyProperties(paymentVOList.get(i), vo);
            message.getRecords().add(vo);
        }
        return message;
    }

    @Override
    public List<PaymentMethodStatVO> revenueByPaymentMethod(String startDate, String endDate) {
        List<PaymentVO> dailyRevenue = paymentFeignClient.getPaymentsByTime(startDate, endDate).getData();
        if (dailyRevenue == null) {
            dailyRevenue = new ArrayList<>();
        }
        List<PaymentMethodStatVO> paymentMethodStatVOList = new ArrayList<>();
        paymentMethodStatVOList.add(PaymentMethodStatVO.builder().method(PaymentMethod.WECHAT.name()).methodName(PaymentMethod.WECHAT.getLabel()).build());
        paymentMethodStatVOList.add(PaymentMethodStatVO.builder().method(PaymentMethod.ALIPAY.name()).methodName(PaymentMethod.ALIPAY.getLabel()).build());
        paymentMethodStatVOList.add(PaymentMethodStatVO.builder().method(PaymentMethod.CASH.name()).methodName(PaymentMethod.CASH.getLabel()).build());
        paymentMethodStatVOList.add(PaymentMethodStatVO.builder().method(PaymentMethod.CARD.name()).methodName(PaymentMethod.CARD.getLabel()).build());
        long total = 0;
        for(PaymentVO revenue: dailyRevenue){
            switch (revenue.getMethod()){
                case "WECHAT":
                    paymentMethodStatVOList.getFirst().setAmount(paymentMethodStatVOList.getFirst().getAmount().add(revenue.getAmount()));
                    paymentMethodStatVOList.getFirst().setCount(paymentMethodStatVOList.getFirst().getCount() + 1);
                    break;
                case "ALIPAY":
                    paymentMethodStatVOList.get(1).setAmount(paymentMethodStatVOList.get(1).getAmount().add(revenue.getAmount()));
                    paymentMethodStatVOList.get(1).setCount(paymentMethodStatVOList.get(1).getCount() + 1);
                    break;
                case "CASH":
                    paymentMethodStatVOList.get(2).setAmount(paymentMethodStatVOList.get(2).getAmount().add(revenue.getAmount()));
                    paymentMethodStatVOList.get(2).setCount(paymentMethodStatVOList.get(2).getCount() + 1);
                    break;
                case "CARD":
                    paymentMethodStatVOList.get(3).setAmount(paymentMethodStatVOList.get(3).getAmount().add(revenue.getAmount()));
                    paymentMethodStatVOList.get(3).setCount(paymentMethodStatVOList.get(3).getCount() + 1);
            }
            total += revenue.getAmount().longValue();
        }
        long finalTotal = total;
        paymentMethodStatVOList.forEach(paymentMethodStatVO -> {
            paymentMethodStatVO.setRate(String.format("%.2f", paymentMethodStatVO.getAmount().doubleValue() / finalTotal * 100));
        });
        return paymentMethodStatVOList;
    }

    @Override
    public List<RoomTypeRevenueVO> revenueByRoomType(String startDate, String endDate) {
        // 1. 获取所有房型数据 + 所有时间范围内订单数据 + 所有房间数据
        Result<PageResult<RoomTypeVO>> roomTypesResult = roomTypeFeignClient.list(1, Integer.MAX_VALUE, "");
        Result<List<OrderVO>> ordersResult = orderFeignClient.listByTime(startDate, endDate);
        Result<PageResult<RoomVO>> roomsResult = roomFeignClient.list(1, Integer.MAX_VALUE, null, "", null, "");

        // 空值安全处理
        List<RoomTypeVO> roomTypeList = Optional.ofNullable(roomTypesResult)
                .map(Result::getData)
                .map(PageResult::getRecords)
                .orElse(Collections.emptyList());

        List<OrderVO> orderList = Optional.ofNullable(ordersResult)
                .map(Result::getData)
                .orElse(Collections.emptyList());

        // 取出所有房间列表
        List<RoomVO> roomList = Optional.ofNullable(roomsResult)
                .map(Result::getData)
                .map(PageResult::getRecords)
                .orElse(Collections.emptyList());

        if (CollectionUtils.isEmpty(roomTypeList) || CollectionUtils.isEmpty(orderList)) {
            return Collections.emptyList();
        }

        // ===================== 核心：按房型分组统计房间状态 =====================
        // Map<房型名称, Map<房间状态, 房间数量>>
        Map<String, Map<String, Long>> roomStatusGroupByRoomType = roomList.stream()
                .filter(room -> room.getRoomTypeName() != null)
                .collect(Collectors.groupingBy(
                        RoomVO::getRoomTypeName,
                        Collectors.groupingBy(
                                RoomVO::getStatus,
                                Collectors.counting()
                        )
                ));

        // 2. 构建【房型名称 -> 房型VO】映射
        Map<String, RoomTypeVO> roomTypeNameMap = roomTypeList.stream()
                .collect(Collectors.toMap(
                        RoomTypeVO::getName,
                        roomType -> roomType,
                        (oldValue, newValue) -> oldValue
                ));

        // 3. 按房型分组订单
        Map<String, List<OrderVO>> orderGroupByRoomType = orderList.stream()
                .filter(order -> Objects.nonNull(order.getRoomTypeName()))
                .collect(Collectors.groupingBy(OrderVO::getRoomTypeName));

        // 4. 组装结果
        List<RoomTypeRevenueVO> resultList = new ArrayList<>();
        for (Map.Entry<String, List<OrderVO>> entry : orderGroupByRoomType.entrySet()) {
            String roomTypeName = entry.getKey();
            List<OrderVO> orders = entry.getValue();

            // 匹配房型ID
            RoomTypeVO roomType = roomTypeNameMap.get(roomTypeName);
            Long roomTypeId = roomType != null ? roomType.getId() : null;

            // 总营收
            BigDecimal totalRevenue = orders.stream()
                    .map(OrderVO::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // ===================== 计算当前房型的真实入住率 =====================
            Map<String, Long> statusCountMap = roomStatusGroupByRoomType.getOrDefault(roomTypeName, Collections.emptyMap());
            long checkedIn = statusCountMap.getOrDefault("入住中", 0L);   // 入住中
            long idle = statusCountMap.getOrDefault("空闲中", 0L);       // 空闲中
            long totalAvailable = checkedIn + idle;                     // 可售房间总数

            String occupancyRate;
            if (totalAvailable <= 0) {
                occupancyRate = "0.00";
            } else {
                // 计算百分比，保留 2 位小数
                double rate = (checkedIn * 1.0 / totalAvailable) * 100;
                occupancyRate = String.format("%.2f", rate);
            }

            // 构建VO
            RoomTypeRevenueVO revenueVO = RoomTypeRevenueVO.builder()
                    .roomTypeId(roomTypeId)
                    .roomTypeName(roomTypeName)
                    .orderCount(orders.size())
                    .revenue(totalRevenue)
                    .roomCount(orders.size())
                    .occupancyRate(occupancyRate)  // 真实入住率
                    .build();

            resultList.add(revenueVO);
        }

        // 按营收倒序
        resultList.sort((a, b) -> b.getRevenue().compareTo(a.getRevenue()));

        return resultList;
    }

    @Override
    public OccupancyTrendVO occupancyTrend(String startDate, String endDate, String type) {
        // 1. 获取所有房间
        Result<PageResult<RoomVO>> roomsResult = roomFeignClient.list(1, Integer.MAX_VALUE, null, "", null, "");
        List<RoomVO> roomList = Optional.ofNullable(roomsResult)
                .map(Result::getData)
                .map(PageResult::getRecords)
                .orElse(Collections.emptyList());

        if (CollectionUtils.isEmpty(roomList)) {
            return OccupancyTrendVO.builder()
                    .dates(Collections.emptyList())
                    .occupancyRates(Collections.emptyList())
                    .availableRooms(0)
                    .avgOccupancyRate("0.00%")
                    .build();
        }

        // 2. 统计当前总可售房间（空闲+入住中）
        long idleCount = roomList.stream().filter(r -> "空闲中".equals(r.getStatus())).count();
        long checkInCount = roomList.stream().filter(r -> "入住中".equals(r.getStatus())).count();
        int availableRooms = (int) (idleCount + checkInCount);

        // 3. 生成日期列表（按日 or 按月）
        DateTimeFormatter formatter;
        List<String> dateList = new ArrayList<>();

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        if ("month".equalsIgnoreCase(type)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate current = start.with(TemporalAdjusters.firstDayOfMonth());
            while (!current.isAfter(end)) {
                dateList.add(current.format(formatter));
                current = current.plusMonths(1);
            }
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate current = start;
            while (!current.isAfter(end)) {
                dateList.add(current.format(formatter));
                current = current.plusDays(1);
            }
        }

        // 4. 计算每日/每月入住率（这里使用当前实时状态，趋势图展示统一值）
        List<String> rateList = new ArrayList<>();
        double totalRate = 0.0;

        for (String date : dateList) {
            double occupancyRate = 0.0;
            if (availableRooms > 0) {
                occupancyRate = (checkInCount * 1.0 / availableRooms) * 100;
            }
            rateList.add(String.format("%.2f", occupancyRate));
            totalRate += occupancyRate;
        }

        // 5. 平均入住率
        String avgRate = "0.00%";
        if (!dateList.isEmpty()) {
            avgRate = String.format("%.2f%%", totalRate / dateList.size());
        }

        // 6. 返回趋势VO
        return OccupancyTrendVO.builder()
                .dates(dateList)
                .occupancyRates(rateList)
                .availableRooms(availableRooms)
                .avgOccupancyRate(avgRate)
                .build();
    }

    @Override
    public List<OccupancyByTypeVO> occupancyByType(String startDate, String endDate) {
        // 1. 获取所有房型、所有房间、所有时间段订单
        Result<PageResult<RoomTypeVO>> roomTypesResult = roomTypeFeignClient.list(1, Integer.MAX_VALUE, "");
        Result<PageResult<RoomVO>> roomsResult = roomFeignClient.list(1, Integer.MAX_VALUE, null, "", null, "");
        Result<List<OrderVO>> ordersResult = orderFeignClient.listByTime(startDate, endDate);

        // 空值处理
        List<RoomTypeVO> roomTypeList = Optional.ofNullable(roomTypesResult)
                .map(Result::getData)
                .map(PageResult::getRecords)
                .orElse(Collections.emptyList());

        List<RoomVO> roomList = Optional.ofNullable(roomsResult)
                .map(Result::getData)
                .map(PageResult::getRecords)
                .orElse(Collections.emptyList());

        List<OrderVO> orderList = Optional.ofNullable(ordersResult)
                .map(Result::getData)
                .orElse(Collections.emptyList());

        if (CollectionUtils.isEmpty(roomTypeList)) {
            return Collections.emptyList();
        }

        // 2. 统计天数（end - start + 1）
        LocalDate sDate = LocalDate.parse(startDate);
        LocalDate eDate = LocalDate.parse(endDate);
        int days = (int) ChronoUnit.DAYS.between(sDate, eDate) + 1;

        // 3. 按房型分组 → 统计每个房型的总房间数
        Map<String, Long> roomCountByType = roomList.stream()
                .filter(room -> room.getRoomTypeName() != null)
                .collect(Collectors.groupingBy(
                        RoomVO::getRoomTypeName,
                        Collectors.counting()
                ));

        Map<String, Integer> occupiedNightsByType = orderList.stream()
                .filter(order -> order.getRoomTypeName() != null)
                .collect(Collectors.groupingBy(
                        OrderVO::getRoomTypeName,
                        Collectors.summingInt(this::calculateRealNights) // 调用自定义方法
                ));

        // 5. 组装每个房型的入住率数据
        List<OccupancyByTypeVO> result = new ArrayList<>();
        for (RoomTypeVO type : roomTypeList) {
            String typeName = type.getName();
            // 总房间数
            int totalRooms = roomCountByType.getOrDefault(typeName, 0L).intValue();
            // 已入住晚数（已按实际/预计计算）
            int occupiedNights = occupiedNightsByType.getOrDefault(typeName, 0);
            // 总可售晚数 = 房间数 × 天数
            int totalNights = totalRooms * days;
            // 入住率
            String occupancyRate = "0.00%";
            if (totalNights > 0) {
                double rate = (occupiedNights * 1.0 / totalNights) * 100;
                occupancyRate = String.format("%.2f%%", rate);
            }

            OccupancyByTypeVO vo = OccupancyByTypeVO.builder()
                    .roomTypeName(typeName)
                    .totalRooms(totalRooms)
                    .occupiedNights(occupiedNights)
                    .totalNights(totalNights)
                    .occupancyRate(occupancyRate)
                    .build();

            result.add(vo);
        }

        // 可选：按入住率降序排序
        result.sort((a, b) -> {
            double rateA = Double.parseDouble(a.getOccupancyRate().replace("%", ""));
            double rateB = Double.parseDouble(b.getOccupancyRate().replace("%", ""));
            return Double.compare(rateB, rateA);
        });

        return result;
    }

    /**
     * 【核心工具方法】计算真实入住晚数
     * 规则：
     * 1. 实际入住、实际退房 都不为空 → 用实际时间计算
     * 2. 任意一个为空 → 用预计入住、预计退房计算
     * 3. 计算方式：退房日期 - 入住日期 = 晚数
     */
    private int calculateRealNights(OrderVO order) {
        LocalDate checkIn;
        LocalDate checkOut;

        // 优先使用实际入住/退房时间
        if (order.getActualCheckIn() != null && order.getActualCheckOut() != null) {
            checkIn = order.getActualCheckIn().toLocalDate();
            checkOut = order.getActualCheckOut().toLocalDate();
        } else {
            // 实际时间为空，使用预计时间
            checkIn = order.getCheckInDate();
            checkOut = order.getCheckOutDate();
        }

        // 防止日期异常
        if (checkIn == null || checkOut == null || checkOut.isBefore(checkIn)) {
            return 0;
        }

        // 计算晚数
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        // 同天入住退房时，回退用预计日期
        if (nights == 0 && order.getCheckInDate() != null && order.getCheckOutDate() != null
                && !order.getCheckOutDate().isBefore(order.getCheckInDate())) {
            nights = (int) ChronoUnit.DAYS.between(order.getCheckInDate(), order.getCheckOutDate());
        }
        return nights;
    }

    @Override
    public PageResult<RefundRecordVO> refundList(Integer page, Integer size, String startDate, String endDate,
                                                 String status, String keyword) {
        Page<RefundRecord> refundRecordPage = refundRecordMapper.selectPage(new Page<>(page, size), new QueryWrapper<RefundRecord>()
                .like(StrUtil.isNotBlank(keyword), "order_no", keyword)
                .eq(StrUtil.isNotBlank(status), "status", status)
                .ge(StrUtil.isNotBlank(startDate), "create_time", startDate)
                .le(StrUtil.isNotBlank(endDate), "create_time", endDate)
        );
        return PageResult.of(refundRecordPage.getTotal(), page,size, BeanUtil.copyToList(refundRecordPage.getRecords(), RefundRecordVO.class));
    }

    @Override
    public RefundDetailVO refundDetail(Long id) {
        // 1. 查询退款记录
        RefundRecord refundRecord = refundRecordMapper.selectById(id);
        if (refundRecord == null) {
            throw new RuntimeException("退款记录不存在");
        }

        // 2. 远程调用获取原支付信息
        Result<List<PaymentVO>> paymentsResult = paymentFeignClient.getPayments(refundRecord.getPaymentId());
        List<PaymentVO> paymentList = Optional.ofNullable(paymentsResult)
                .map(Result::getData)
                .orElse(Collections.emptyList());

        // 3. 取第一条支付记录（原支付单）
        PaymentVO payment = paymentList.stream().findFirst().orElse(null);

        // 4. 组装退款详情 VO
        return RefundDetailVO.builder()
                .id(refundRecord.getId())
                .orderId(refundRecord.getOrderId())
                .orderNo(refundRecord.getOrderNo())
                // 原支付单号
                .paymentNo(payment != null ? payment.getPaymentNo() : null)
                // 原支付金额
                .originalAmount(payment != null ? payment.getAmount() : null)
                // 原支付方式
                .originalMethod(payment != null ? payment.getMethod() : null)
                // 退款金额
                .refundAmount(refundRecord.getRefundAmount())
                // 退款原因
                .reason(refundRecord.getReason())
                // 退款状态
                .status(refundRecord.getStatus())
                // 操作人姓名（你需要自己实现根据 operatorId 获取用户名）
                .operatorName(UserContext.getUsername())
                // 创建时间
                .createTime(refundRecord.getCreateTime())
                .build();
    }

    @Override
    public void addRefundRecord(RefundRecordVO refundRecordVO) {
        if (refundRecordVO == null) {
            throw new IllegalArgumentException("退款记录参数不能为空");
        }
        if (StrUtil.isBlank(refundRecordVO.getOrderNo())) {
            throw new RuntimeException("订单号不能为空");
        }
        RefundRecord refundRecord = new RefundRecord();
        BeanUtil.copyProperties(refundRecordVO, refundRecord);
        refundRecordMapper.insert(refundRecord);
    }

    @Override
    public void exportReport(String type, String startDate, String endDate, String format) {
        // 1. 获取 response
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = null;
        if (attributes != null) {
            response = attributes.getResponse();
        }
        if (response == null) {
            throw new RuntimeException("获取响应对象失败");
        }

        // 2. 默认格式 xlsx
        if (format == null || format.isBlank()) {
            format = "xlsx";
        }

        // 3. 根据 type 获取对应报表数据
        List<?> dataList;
        String fileName = switch (type) {
            case "revenue" -> {
                dataList = revenueByRoomType(startDate, endDate); // 你已有的房型营收报表
                yield "房型营收报表_" + startDate + "_" + endDate;
            }
            case "order" -> {
                dataList = orderFeignClient.listByTime(startDate, endDate).getData(); // 订单明细
                yield "订单明细报表_" + startDate + "_" + endDate;
            }
            case "refund" -> {
                dataList = refundRecordMapper.selectList(
                        new LambdaQueryWrapper<RefundRecord>()
                                .between(RefundRecord::getCreateTime, startDate, endDate)
                );// 退款汇总
                yield "退款汇总报表_" + startDate + "_" + endDate;
            }
            default -> throw new RuntimeException("不支持的报表类型");
        };

        // 4. 设置下载响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 5. 通用样式（居中）
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(headStyle, contentStyle);

        // 6. 写出文件到浏览器
        try {
            EasyExcel.write(response.getOutputStream())
                    .registerWriteHandler(styleStrategy)
                    .autoCloseStream(true)
                    .sheet("报表数据")
                    .doWrite(dataList);
        } catch (IOException e) {
            throw new RuntimeException("导出报表失败", e);
        }
    }

    // ==================== 从订单数据实时计算（daily_revenue 表为空时的降级方案） ====================

    private RevenueSummaryVO computeSummaryFromOrders(LocalDate now, LocalDate firstDayOfYear) {
        Result<List<OrderVO>> ordersResult = orderFeignClient.listByTime(
                firstDayOfYear.toString(), now.toString());
        List<OrderVO> orders = Optional.ofNullable(ordersResult)
                .map(Result::getData)
                .orElse(Collections.emptyList());

        if (CollectionUtils.isEmpty(orders)) {
            return RevenueSummaryVO.builder()
                    .today(TodaySummary.builder().build())
                    .yesterday(TodaySummary.builder().build())
                    .thisMonth(MonthSummary.builder().build())
                    .thisYear(YearSummary.builder().build())
                    .build();
        }

        LocalDate yesterday = now.minusDays(1);

        // 今日订单（按入住日期）
        List<OrderVO> todayOrders = orders.stream()
                .filter(o -> o.getCheckInDate() != null && o.getCheckInDate().equals(now))
                .toList();

        // 昨日订单
        List<OrderVO> yesterdayOrders = orders.stream()
                .filter(o -> o.getCheckInDate() != null && o.getCheckInDate().equals(yesterday))
                .toList();

        // 本月订单
        List<OrderVO> monthOrders = orders.stream()
                .filter(o -> o.getCheckInDate() != null
                        && o.getCheckInDate().getYear() == now.getYear()
                        && o.getCheckInDate().getMonth() == now.getMonth())
                .toList();

        // 今日汇总
        TodaySummary today = null;
        if (!todayOrders.isEmpty()) {
            today = buildTodaySummaryFromOrders(todayOrders, now);
        }

        // 昨日汇总
        TodaySummary yesterdaySummary = null;
        if (!yesterdayOrders.isEmpty()) {
            yesterdaySummary = buildTodaySummaryFromOrders(yesterdayOrders, yesterday);
        }

        // 本月汇总
        BigDecimal monthRoomRev = sumField(monthOrders, OrderVO::getRoomTotal);
        BigDecimal monthExtraRev = sumField(monthOrders, OrderVO::getExtraTotal);
        BigDecimal monthTotal = monthRoomRev.add(monthExtraRev);
        int daysInMonth = now.getDayOfMonth();
        MonthSummary thisMonth = MonthSummary.builder()
                .month(now.getMonth().toString())
                .roomRevenue(monthRoomRev)
                .extraRevenue(monthExtraRev)
                .totalRevenue(monthTotal)
                .orderCount(monthOrders.size())
                .avgDailyRevenue(daysInMonth > 0
                        ? monthTotal.divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                .build();

        // 年度汇总
        BigDecimal yearRoomRev = sumField(orders, OrderVO::getRoomTotal);
        BigDecimal yearExtraRev = sumField(orders, OrderVO::getExtraTotal);
        YearSummary thisYear = YearSummary.builder()
                .year(String.valueOf(now.getYear()))
                .roomRevenue(yearRoomRev)
                .extraRevenue(yearExtraRev)
                .totalRevenue(yearRoomRev.add(yearExtraRev))
                .orderCount(orders.size())
                .build();

        return RevenueSummaryVO.builder()
                .today(today)
                .yesterday(yesterdaySummary)
                .thisMonth(thisMonth)
                .thisYear(thisYear)
                .build();
    }

    private TodaySummary buildTodaySummaryFromOrders(List<OrderVO> dayOrders, LocalDate date) {
        BigDecimal roomRev = sumField(dayOrders, OrderVO::getRoomTotal);
        BigDecimal extraRev = sumField(dayOrders, OrderVO::getExtraTotal);
        // 今日入住：当天计划入住房（checkInDate == 今天），非仅已实际办理入住的
        int checkIn = (int) dayOrders.stream()
                .filter(o -> o.getCheckInDate() != null && date.equals(o.getCheckInDate()))
                .count();
        // 今日退房：当天计划退房（checkOutDate == 今天）
        int checkOut = (int) dayOrders.stream()
                .filter(o -> o.getCheckOutDate() != null && date.equals(o.getCheckOutDate()))
                .count();
        return TodaySummary.builder()
                .date(date.toString())
                .roomRevenue(roomRev)
                .extraRevenue(extraRev)
                .totalRevenue(roomRev.add(extraRev))
                .orderCount(dayOrders.size())
                .checkInCount(checkIn)
                .checkOutCount(checkOut)
                .occupancyRate("0.00%")
                .build();
    }

    private TodaySummary buildTodaySummary(DailyRevenue revenue, LocalDate date) {
        return TodaySummary.builder()
                .date(date.toString())
                .roomRevenue(revenue.getRoomRevenue())
                .extraRevenue(revenue.getExtraRevenue())
                .totalRevenue(revenue.getTotalRevenue())
                .orderCount(revenue.getOrderCount())
                .checkInCount(revenue.getCheckInCount())
                .checkOutCount(revenue.getCheckOutCount())
                .occupancyRate(String.format("%.2f%%",
                        (revenue.getOrderCount() == null || revenue.getOrderCount() == 0)
                                ? 0.0
                                : (revenue.getCheckInCount() * 100.0) / revenue.getOrderCount()))
                .build();
    }

    private DailyTrendVO computeDailyFromOrders(String startDate, String endDate) {
        Result<List<OrderVO>> ordersResult = orderFeignClient.listByTime(startDate, endDate);
        List<OrderVO> orders = Optional.ofNullable(ordersResult)
                .map(Result::getData)
                .orElse(Collections.emptyList());

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // 按入住日期分组
        Map<LocalDate, List<OrderVO>> ordersByDate = orders.stream()
                .filter(o -> o.getCheckInDate() != null)
                .collect(Collectors.groupingBy(OrderVO::getCheckInDate));

        List<String> dates = new ArrayList<>();
        List<BigDecimal> roomRevenueList = new ArrayList<>();
        List<BigDecimal> extraRevenueList = new ArrayList<>();
        List<BigDecimal> totalRevenueList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current.toString());
            List<OrderVO> dayOrders = ordersByDate.getOrDefault(current, Collections.emptyList());
            BigDecimal roomRev = sumField(dayOrders, OrderVO::getRoomTotal);
            BigDecimal extraRev = sumField(dayOrders, OrderVO::getExtraTotal);
            roomRevenueList.add(roomRev);
            extraRevenueList.add(extraRev);
            totalRevenueList.add(roomRev.add(extraRev));
            orderCountList.add(dayOrders.size());
            current = current.plusDays(1);
        }

        return DailyTrendVO.builder()
                .dates(dates)
                .roomRevenue(roomRevenueList)
                .extraRevenue(extraRevenueList)
                .totalRevenue(totalRevenueList)
                .orderCount(orderCountList)
                .build();
    }

    private BigDecimal sumField(List<OrderVO> list, java.util.function.Function<OrderVO, BigDecimal> getter) {
        return list.stream()
                .map(o -> getter.apply(o) != null ? getter.apply(o) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
