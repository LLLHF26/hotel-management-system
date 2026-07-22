package com.lhf.hotel.order.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.order.model.dto.ServiceRequestDTO;
import com.lhf.hotel.order.model.entity.ServiceRequest;

import java.util.List;

public interface ServiceRequestService {

    /** 客户发起服务呼叫（自动关联当前在住订单的房间） */
    ServiceRequest create(ServiceRequestDTO dto);

    /** 客户查看自己的服务呼叫 */
    List<ServiceRequest> listMine();

    /** 管理侧：分页查询 */
    PageResult<ServiceRequest> pageAdmin(String status, int page, int size);

    /** 管理侧：处理 / 取消 */
    ServiceRequest handle(Long id, String status, String handleRemark);
}
