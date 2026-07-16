package com.lhf.hotel.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.asserts.Assert;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.user.mapper.CustomerMapper;
import com.lhf.hotel.user.mapper.PointsLogMapper;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.entity.Customer;
import com.lhf.hotel.user.model.entity.PointsLog;
import com.lhf.hotel.user.model.vo.CustomerVO;
import com.lhf.hotel.user.model.vo.PointsLogVO;
import com.lhf.hotel.user.service.CustomerService;
import com.lhf.hotel.user.support.UserConverters;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final PointsLogMapper pointsLogMapper;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerMapper customerMapper, PointsLogMapper pointsLogMapper,
                               PasswordEncoder passwordEncoder) {
        this.customerMapper = customerMapper;
        this.pointsLogMapper = pointsLogMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<CustomerVO> listCustomers(int page, int size, String keyword, String memberLevel, Integer status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Customer::getRealName, keyword)
                    .or()
                    .like(Customer::getPhone, keyword));
        }
        if (StringUtils.hasText(memberLevel)) {
            wrapper.eq(Customer::getMemberLevel, memberLevel);
        }
        if (status != null) {
            wrapper.eq(Customer::getStatus, status);
        }
        wrapper.orderByDesc(Customer::getCreateTime);

        Page<Customer> result = customerMapper.selectPage(new Page<>(page, size), wrapper);
        List<CustomerVO> records = result.getRecords().stream()
                .map(UserConverters::toCustomerVO)
                .toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public CustomerVO getCustomerById(Long id) {
        Customer customer = customerMapper.selectById(id);
        Assert.notNull(customer, "会员不存在");
        return UserConverters.toCustomerVO(customer);
    }

    @Override
    public Long register(CustomerRegisterDTO dto) {
        long exists = customerMapper.selectCount(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, dto.getPhone()));
        Assert.isFalse(exists > 0, "手机号已注册");

        Customer customer = new Customer();
        customer.setRealName(dto.getRealName());
        customer.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getPassword())) {
            customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        customer.setIdCard(dto.getIdCard());
        customer.setGender(dto.getGender());
        customer.setPoints(0);
        customer.setMemberLevel("NORMAL");
        customer.setStatus(1);
        customerMapper.insert(customer);
        return customer.getId();
    }

    @Override
    public void updateCustomer(Long id, CustomerUpdateDTO dto) {
        Customer customer = customerMapper.selectById(id);
        Assert.notNull(customer, "会员不存在");
        if (dto.getRealName() != null) {
            customer.setRealName(dto.getRealName());
        }
        if (dto.getIdCard() != null) {
            customer.setIdCard(dto.getIdCard());
        }
        if (dto.getGender() != null) {
            customer.setGender(dto.getGender());
        }
        if (dto.getAvatar() != null) {
            customer.setAvatar(dto.getAvatar());
        }
        customerMapper.updateById(customer);
    }

    @Override
    public void updateStatus(Long id, StatusDTO dto) {
        Customer customer = customerMapper.selectById(id);
        Assert.notNull(customer, "会员不存在");
        customer.setStatus(dto.getStatus());
        customerMapper.updateById(customer);
    }

    @Override
    public PageResult<PointsLogVO> getPointsLog(Long customerId, int page, int size) {
        Assert.notNull(customerMapper.selectById(customerId), "会员不存在");

        Page<PointsLog> result = pointsLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<PointsLog>()
                        .eq(PointsLog::getCustomerId, customerId)
                        .orderByDesc(PointsLog::getCreateTime));

        List<PointsLogVO> records = result.getRecords().stream()
                .map(UserConverters::toPointsLogVO)
                .toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public void addPoints(Long customerId, Integer points, String reason) {
        Customer customer = customerMapper.selectById(customerId);
        Assert.notNull(customer, "会员不存在");
        int newBalance = customer.getPoints() + points;
        customer.setPoints(newBalance);
        customerMapper.updateById(customer);

        PointsLog log = new PointsLog();
        log.setCustomerId(customerId);
        log.setType(points >= 0 ? "EARN" : "DEDUCT");
        log.setPoints(points);
        log.setBalanceAfter(newBalance);
        log.setReason(reason);
        pointsLogMapper.insert(log);
    }

    @Override
    public void addConsumed(Long customerId, java.math.BigDecimal amount) {
        Customer customer = customerMapper.selectById(customerId);
        Assert.notNull(customer, "会员不存在");
        java.math.BigDecimal current = customer.getTotalConsumed() != null ? customer.getTotalConsumed() : java.math.BigDecimal.ZERO;
        customer.setTotalConsumed(current.add(amount));
        customerMapper.updateById(customer);
    }

    @Override
    public CustomerVO getProfile(Long customerId) {
        return getCustomerById(customerId);
    }

    @Override
    public void updateProfile(Long customerId, CustomerUpdateDTO dto) {
        updateCustomer(customerId, dto);
    }

    @Override
    public void changePassword(Long customerId, PasswordDTO dto) {
        Customer customer = customerMapper.selectById(customerId);
        Assert.notNull(customer, "会员不存在");
        Assert.hasText(customer.getPassword(), "尚未设置密码");
        Assert.isTrue(passwordEncoder.matches(dto.getOldPassword(), customer.getPassword()), "旧密码错误");
        customer.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        customerMapper.updateById(customer);
    }
}
