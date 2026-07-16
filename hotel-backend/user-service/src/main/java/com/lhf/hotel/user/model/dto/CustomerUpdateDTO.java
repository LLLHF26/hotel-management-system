package com.lhf.hotel.user.model.dto;

import lombok.Data;

@Data
public class CustomerUpdateDTO {

    private String realName;
    private String idCard;
    private Integer gender;
    private String avatar;
}
