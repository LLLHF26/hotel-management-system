package com.lhf.hotel.order.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.order.model.dto.ProductDTO;
import com.lhf.hotel.order.model.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductService {

    /** 客户侧：可用商品列表（含积分抵扣配置） */
    Map<String, Object> listAvailableWithConfig();

    /** 管理侧：分页列表 */
    PageResult<Product> pageAdmin(String keyword, int page, int size);

    Product getById(Long id);

    void create(ProductDTO dto);

    void update(Long id, ProductDTO dto);

    void remove(Long id);
}
