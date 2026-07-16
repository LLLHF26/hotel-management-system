package com.lhf.hotel.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.asserts.Assert;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.room.mapper.MaintenanceRecordMapper;
import com.lhf.hotel.room.mapper.RoomMapper;
import com.lhf.hotel.room.model.dto.MaintenanceCompleteDTO;
import com.lhf.hotel.room.model.dto.MaintenanceStartDTO;
import com.lhf.hotel.room.model.entity.MaintenanceRecord;
import com.lhf.hotel.room.model.entity.Room;
import com.lhf.hotel.room.model.vo.MaintenanceRecordVO;
import com.lhf.hotel.room.service.MaintenanceService;
import com.lhf.hotel.room.support.RoomConverters;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRecordMapper recordMapper;
    private final RoomMapper roomMapper;

    public MaintenanceServiceImpl(MaintenanceRecordMapper recordMapper, RoomMapper roomMapper) {
        this.recordMapper = recordMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public Long start(Long roomId, MaintenanceStartDTO dto) {
        Room room = roomMapper.selectById(roomId);
        Assert.notNull(room, "房间不存在");
        Assert.isTrue("空闲中".equals(room.getStatus()), "仅空闲中的房间可设为维修中");

        MaintenanceRecord record = new MaintenanceRecord();
        record.setRoomId(roomId);
        record.setRoomNumber(room.getRoomNumber());
        record.setReason(dto.getReason());
        record.setStatus("维修中");
        record.setStartTime(LocalDateTime.now());
        record.setCost(BigDecimal.ZERO);
        recordMapper.insert(record);

        room.setStatus("维修中");
        roomMapper.updateById(room);

        return record.getId();
    }

    @Override
    public void complete(Long roomId, MaintenanceCompleteDTO dto) {
        Room room = roomMapper.selectById(roomId);
        Assert.notNull(room, "房间不存在");
        Assert.isTrue("维修中".equals(room.getStatus()), "该房间不在维修中");

        MaintenanceRecord record = findActiveRecord(roomId);
        Assert.notNull(record, "未找到进行中的维修记录");
        record.setStatus("已完成");
        record.setEndTime(LocalDateTime.now());
        if (dto.getCost() != null) {
            record.setCost(dto.getCost());
        }
        recordMapper.updateById(record);

        room.setStatus("空闲中");
        roomMapper.updateById(room);
    }

    @Override
    public PageResult<MaintenanceRecordVO> records(int page, int size, Long roomId, String status,
                                                    String startDate, String endDate) {
        LambdaQueryWrapper<MaintenanceRecord> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(MaintenanceRecord::getRoomId, roomId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(MaintenanceRecord::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(MaintenanceRecord::getStartTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.lt(MaintenanceRecord::getStartTime, LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(MaintenanceRecord::getStartTime);

        Page<MaintenanceRecord> result = recordMapper.selectPage(new Page<>(page, size), wrapper);
        List<MaintenanceRecordVO> records = result.getRecords().stream()
                .map(RoomConverters::toMaintenanceRecordVO)
                .toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    private MaintenanceRecord findActiveRecord(Long roomId) {
        return recordMapper.selectOne(
                new LambdaQueryWrapper<MaintenanceRecord>()
                        .eq(MaintenanceRecord::getRoomId, roomId)
                        .eq(MaintenanceRecord::getStatus, "维修中")
                        .orderByDesc(MaintenanceRecord::getStartTime)
                        .last("LIMIT 1"));
    }
}
