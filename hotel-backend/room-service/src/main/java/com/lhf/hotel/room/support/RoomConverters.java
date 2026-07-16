package com.lhf.hotel.room.support;

import com.alibaba.fastjson2.JSON;
import com.lhf.hotel.room.model.entity.CleaningTask;
import com.lhf.hotel.room.model.entity.MaintenanceRecord;
import com.lhf.hotel.room.model.entity.Room;
import com.lhf.hotel.room.model.entity.RoomType;
import com.lhf.hotel.room.model.vo.CleaningTaskVO;
import com.lhf.hotel.room.model.vo.MaintenanceRecordVO;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import com.lhf.hotel.room.model.vo.RoomVO;
import com.lhf.hotel.room.model.vo.TaskInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public final class RoomConverters {

    private static final int CLEANING_DURATION_MINUTES = 5;

    private RoomConverters() {
    }

    public static List<String> parseImages(String images) {
        if (images == null || images.isBlank()) {
            return Collections.emptyList();
        }
        String trimmed = images.trim();
        if (trimmed.startsWith("[")) {
            return JSON.parseArray(trimmed, String.class);
        }
        return List.of(trimmed.split(","));
    }

    public static String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(images);
    }

    public static RoomTypeVO toRoomTypeVO(RoomType entity, boolean includeDetail) {
        RoomTypeVO.RoomTypeVOBuilder builder = RoomTypeVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .area(entity.getArea())
                .bedType(entity.getBedType())
                .maxGuests(entity.getMaxGuests())
                .price(entity.getPrice())
                .coverImage(entity.getCoverImage())
                .amenities(entity.getAmenities())
                .sortOrder(entity.getSortOrder());
        if (includeDetail) {
            builder.images(parseImages(entity.getImages()))
                    .createTime(entity.getCreateTime())
                    .updateTime(entity.getUpdateTime());
        }
        return builder.build();
    }

    public static RoomVO toRoomVO(Room room, RoomType roomType) {
        return RoomVO.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomTypeId(room.getRoomTypeId())
                .roomTypeName(roomType != null ? roomType.getName() : "")
                .floor(room.getFloor())
                .status(room.getStatus())
                .price(roomType != null ? roomType.getPrice() : null)
                .description(room.getDescription())
                .createTime(room.getCreateTime())
                .updateTime(room.getUpdateTime())
                .build();
    }

    public static TaskInfo toCleaningTaskInfo(CleaningTask task) {
        int elapsed = (int) Duration.between(task.getStartTime(), LocalDateTime.now()).toMinutes();
        int remain = Math.max(0, CLEANING_DURATION_MINUTES - elapsed);
        return TaskInfo.builder()
                .taskId(task.getId())
                .type("cleaning")
                .cleanerName(task.getCleanerName())
                .startTime(task.getStartTime())
                .remainMinutes(remain)
                .build();
    }

    public static TaskInfo toMaintenanceTaskInfo(MaintenanceRecord record) {
        return TaskInfo.builder()
                .taskId(record.getId())
                .type("maintenance")
                .reason(record.getReason())
                .startTime(record.getStartTime())
                .build();
    }

    public static CleaningTaskVO toCleaningTaskVO(CleaningTask task, String roomTypeName) {
        CleaningTaskVO.CleaningTaskVOBuilder builder = CleaningTaskVO.builder()
                .id(task.getId())
                .roomId(task.getRoomId())
                .roomNumber(task.getRoomNumber())
                .roomTypeName(roomTypeName)
                .cleanerId(task.getCleanerId())
                .cleanerName(task.getCleanerName())
                .status(task.getStatus())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime());
        if ("已完成".equals(task.getStatus()) && task.getStartTime() != null && task.getEndTime() != null) {
            builder.durationMinutes((int) Duration.between(task.getStartTime(), task.getEndTime()).toMinutes());
        } else if ("打扫中".equals(task.getStatus()) && task.getStartTime() != null) {
            builder.elapsedMinutes((int) Duration.between(task.getStartTime(), LocalDateTime.now()).toMinutes());
        }
        return builder.build();
    }

    public static MaintenanceRecordVO toMaintenanceRecordVO(MaintenanceRecord record) {
        return MaintenanceRecordVO.builder()
                .id(record.getId())
                .roomId(record.getRoomId())
                .roomNumber(record.getRoomNumber())
                .reason(record.getReason())
                .status(record.getStatus())
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .cost(record.getCost())
                .build();
    }
}
