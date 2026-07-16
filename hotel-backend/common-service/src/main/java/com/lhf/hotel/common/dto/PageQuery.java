package com.lhf.hotel.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页查询参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {

    private int page = 1;
    private int size = 10;

    public int getOffset() {
        return (page - 1) * size;
    }
}
