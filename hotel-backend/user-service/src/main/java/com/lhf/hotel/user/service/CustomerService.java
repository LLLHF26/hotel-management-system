package com.lhf.hotel.user.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.vo.CustomerVO;
import com.lhf.hotel.user.model.vo.PointsLogVO;

public interface CustomerService {

    // ========== 会员管理（管理端） ==========

    PageResult<CustomerVO> listCustomers(int page, int size, String keyword, String memberLevel, Integer status);

    CustomerVO getCustomerById(Long id);

    Long register(CustomerRegisterDTO dto);

    void updateCustomer(Long id, CustomerUpdateDTO dto);

    void updateStatus(Long id, StatusDTO dto);

    PageResult<PointsLogVO> getPointsLog(Long customerId, int page, int size);

    void addPoints(Long customerId, Integer points, String reason);

    void addConsumed(Long customerId, java.math.BigDecimal amount);

    // ========== 会员个人中心（客户端） ==========

    CustomerVO getProfile(Long customerId);

    void updateProfile(Long customerId, CustomerUpdateDTO dto);

    void changePassword(Long customerId, PasswordDTO dto);
}
