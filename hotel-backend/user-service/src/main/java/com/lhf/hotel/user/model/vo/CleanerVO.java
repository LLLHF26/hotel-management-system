package com.lhf.hotel.user.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CleanerVO {

    private Long id;
    private String realName;
    private String phone;
    private Integer currentTaskCount;
}
