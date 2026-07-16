package com.lhf.hotel.user.model.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {

    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String role;
}
