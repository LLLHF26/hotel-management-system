package com.lhf.hotel.user.support;

import com.lhf.hotel.common.dto.CleaningTaskDTO;
import com.lhf.hotel.user.model.entity.Customer;
import com.lhf.hotel.user.model.entity.PointsLog;
import com.lhf.hotel.user.model.entity.User;
import com.lhf.hotel.user.model.vo.CleanerTaskVO;
import com.lhf.hotel.user.model.vo.CustomerVO;
import com.lhf.hotel.user.model.vo.PointsLogVO;
import com.lhf.hotel.user.model.vo.UserVO;

public final class UserConverters {

    private UserConverters() {
    }

    public static UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    public static CustomerVO toCustomerVO(Customer customer) {
        return CustomerVO.builder()
                .id(customer.getId())
                .realName(customer.getRealName())
                .idCard(customer.getIdCard())
                .phone(customer.getPhone())
                .gender(customer.getGender())
                .avatar(customer.getAvatar())
                .points(customer.getPoints())
                .totalConsumed(customer.getTotalConsumed())
                .memberLevel(customer.getMemberLevel())
                .status(customer.getStatus())
                .createTime(customer.getCreateTime())
                .build();
    }

    public static PointsLogVO toPointsLogVO(PointsLog log) {
        String typeName = switch (log.getType()) {
            case "EARN" -> "获得";
            case "CONSUME" -> "消费";
            default -> log.getType();
        };
        return PointsLogVO.builder()
                .id(log.getId())
                .type(log.getType())
                .typeName(typeName)
                .points(log.getPoints())
                .balanceAfter(log.getBalanceAfter())
                .reason(log.getReason())
                .createTime(log.getCreateTime())
                .build();
    }

    public static CleanerTaskVO toCleanerTaskVO(CleaningTaskDTO dto) {
        return CleanerTaskVO.builder()
                .taskId(dto.getTaskId())
                .roomId(dto.getRoomId())
                .roomNumber(dto.getRoomNumber())
                .status(dto.getStatus())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}
