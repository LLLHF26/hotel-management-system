package com.lhf.hotel.order.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 分类：食物 / 饮用水 / 日用品 / 其他 */
    private String category;

    /** 商品名称 */
    private String name;

    /** 单价 */
    private BigDecimal price;

    /** 封面图 */
    private String image;

    /** 计价单位：份 / 瓶 / 个 / 盒 */
    private String unit;

    /** 库存（-1 表示不限量） */
    private Integer stock;

    /** 状态：上架 / 下架 */
    private String status;

    /** 描述 */
    private String description;

    /** 排序（越小越靠前） */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
