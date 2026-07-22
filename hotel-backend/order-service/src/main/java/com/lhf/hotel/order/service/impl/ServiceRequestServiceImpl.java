package com.lhf.hotel.order.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.exception.BusinessException;
import com.lhf.hotel.common.util.UserContext;
import com.lhf.hotel.order.model.dto.ServiceRequestDTO;
import com.lhf.hotel.order.model.entity.Orders;
import com.lhf.hotel.order.model.entity.ServiceRequest;
import com.lhf.hotel.order.mapper.OrdersMapper;
import com.lhf.hotel.order.model.mapper.ServiceRequestMapper;
import com.lhf.hotel.order.service.ServiceRequestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceRequestServiceImpl extends ServiceImpl<ServiceRequestMapper, ServiceRequest> implements ServiceRequestService {

    private final OrdersMapper ordersMapper;

    public ServiceRequestServiceImpl(OrdersMapper ordersMapper) {
        this.ordersMapper = ordersMapper;
    }

    @Override
    public ServiceRequest create(ServiceRequestDTO dto) {
        Long customerId = UserContext.getUserId();
        if (customerId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 关联当前在住订单的房间信息
        Orders activeOrder = ordersMapper.selectOne(Wrappers.<Orders>lambdaQuery()
                .eq(Orders::getCustomerId, customerId)
                .eq(Orders::getStatus, "已入住")
                .orderByDesc(Orders::getCreateTime)
                .last("LIMIT 1"));

        ServiceRequest req = new ServiceRequest();
        req.setCustomerId(customerId);
        req.setType(dto.getType());
        req.setRemark(dto.getRemark());
        req.setStatus("待处理");
        if (activeOrder != null) {
            req.setCustomerName(activeOrder.getCustomerName());
            req.setRoomId(activeOrder.getRoomId());
            req.setRoomNumber(activeOrder.getRoomNumber());
        }
        baseMapper.insert(req);
        return req;
    }

    @Override
    public List<ServiceRequest> listMine() {
        Long customerId = UserContext.getUserId();
        if (customerId == null) {
            throw new BusinessException(401, "未登录");
        }
        return lambdaQuery()
                .eq(ServiceRequest::getCustomerId, customerId)
                .orderByDesc(ServiceRequest::getCreateTime)
                .list();
    }

    @Override
    public PageResult<ServiceRequest> pageAdmin(String status, int page, int size) {
        IPage<ServiceRequest> p = new Page<>(page, size);
        p = lambdaQuery()
                .eq(status != null && !status.isBlank(), ServiceRequest::getStatus, status)
                .orderByDesc(ServiceRequest::getCreateTime)
                .page(p);
        return PageResult.of(p.getTotal(), page, size, p.getRecords());
    }

    @Override
    public ServiceRequest handle(Long id, String status, String handleRemark) {
        ServiceRequest req = baseMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(404, "服务呼叫不存在");
        }
        if (!"已处理".equals(status) && !"已取消".equals(status)) {
            throw new BusinessException(400, "处理状态只能是 已处理 / 已取消");
        }
        req.setStatus(status);
        req.setHandleRemark(handleRemark);
        req.setHandlerId(UserContext.getUserId());
        req.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(req);
        return req;
    }
}
