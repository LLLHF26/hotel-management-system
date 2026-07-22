package com.lhf.hotel.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshVO {

    @Schema(description = "令牌")
    private String token;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;
}
