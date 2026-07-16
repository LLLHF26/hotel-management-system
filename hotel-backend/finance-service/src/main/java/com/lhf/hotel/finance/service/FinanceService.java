package com.lhf.hotel.finance.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.model.vo.RefundRecordVO;
import com.lhf.hotel.finance.model.vo.*;

import java.util.List;

public interface FinanceService {

    RevenueSummaryVO revenueSummary();

    DailyTrendVO revenueDaily(String startDate, String endDate);

    List<MonthlyRevenueItemVO> revenueMonthly(Integer year);

    RevenueRangeVO revenueRange(String startDate, String endDate);

    PageResult<RevenueDetailVO> revenueDetail(Integer page, Integer size, String startDate, String endDate,
                                              String paymentMethod, String orderNo, String roomNumber);

    List<PaymentMethodStatVO> revenueByPaymentMethod(String startDate, String endDate);

    List<RoomTypeRevenueVO> revenueByRoomType(String startDate, String endDate);

    OccupancyTrendVO occupancyTrend(String startDate, String endDate, String type);

    List<OccupancyByTypeVO> occupancyByType(String startDate, String endDate);

    PageResult<RefundRecordVO> refundList(Integer page, Integer size, String startDate, String endDate,
                                          String status, String keyword);

    RefundDetailVO refundDetail(Long id);

    void addRefundRecord(RefundRecordVO refundRecordVO);

    void exportReport(String type, String startDate, String endDate, String format);


}
