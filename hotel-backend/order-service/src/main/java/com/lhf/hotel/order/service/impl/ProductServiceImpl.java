package com.lhf.hotel.order.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.exception.BusinessException;
import com.lhf.hotel.order.feign.systemService.SystemSettingFeignClient;
import com.lhf.hotel.order.model.dto.ProductDTO;
import com.lhf.hotel.order.model.entity.Product;
import com.lhf.hotel.order.model.mapper.ProductMapper;
import com.lhf.hotel.order.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    /** 积分抵扣默认比例：1 积分可抵扣的金额（元） */
    private static final BigDecimal DEFAULT_POINTS_RATIO = new BigDecimal("0.1");

    /** 积分抵扣开关在 system_setting 中的配置键 */
    private static final String KEY_POINTS_RATIO = "points_discount_ratio";
    private static final String KEY_POINTS_ENABLED = "points_discount_enabled";

    @Override
    public Map<String, Object> listAvailableWithConfig() {
        List<Product> products = lambdaQuery()
                .eq(Product::getStatus, "上架")
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getCreateTime)
                .list();

        Map<String, Object> config = readPointsConfig();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("products", products);
        result.put("pointsDiscountEnabled", config.get("enabled"));
        result.put("pointsDiscountRatio", config.get("ratio"));
        return result;
    }

    @Override
    public PageResult<Product> pageAdmin(String keyword, int page, int size) {
        IPage<Product> p = new Page<>(page, size);
        p = lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), Product::getName, keyword)
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getCreateTime)
                .page(p);
        return PageResult.of(p.getTotal(), page, size, p.getRecords());
    }

    @Override
    public Product getById(Long id) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    @Override
    public void create(ProductDTO dto) {
        Product product = new Product();
        copyToEntity(dto, product);
        baseMapper.insert(product);
    }

    @Override
    public void update(Long id, ProductDTO dto) {
        Product product = getById(id);
        copyToEntity(dto, product);
        product.setId(id);
        baseMapper.updateById(product);
    }

    @Override
    public void remove(Long id) {
        if (baseMapper.selectById(id) == null) {
            throw new BusinessException(404, "商品不存在");
        }
        baseMapper.deleteById(id);
    }

    private void copyToEntity(ProductDTO dto, Product product) {
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setImage(dto.getImage());
        product.setUnit(dto.getUnit());
        product.setStock(dto.getStock() == null ? -1 : dto.getStock());
        product.setStatus(dto.getStatus() == null ? "上架" : dto.getStatus());
        product.setDescription(dto.getDescription());
        product.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
    }

    /** 读取积分抵扣配置（比例 + 开关），失败时使用默认值 */
    Map<String, Object> readPointsConfig() {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("ratio", DEFAULT_POINTS_RATIO);
        fallback.put("enabled", true);
        try {
            SystemSettingFeignClient settingFeign = SpringUtil.getBean(SystemSettingFeignClient.class);
            var res = settingFeign.listSettings();
            if (res == null || res.getData() == null) {
                return fallback;
            }
            BigDecimal ratio = DEFAULT_POINTS_RATIO;
            boolean enabled = true;
            for (Map<String, Object> item : res.getData()) {
                String key = String.valueOf(item.get("key"));
                String value = item.get("value") == null ? null : String.valueOf(item.get("value"));
                if (KEY_POINTS_RATIO.equals(key) && value != null && !value.isBlank()) {
                    ratio = new BigDecimal(value);
                } else if (KEY_POINTS_ENABLED.equals(key) && value != null) {
                    enabled = "true".equalsIgnoreCase(value) || "1".equals(value);
                }
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("ratio", ratio);
            result.put("enabled", enabled);
            return result;
        } catch (Exception e) {
            return fallback;
        }
    }
}
