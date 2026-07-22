package com.lhf.hotel.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.asserts.Assert;
import com.lhf.hotel.common.dto.RoomStatusChangeDTO;
import com.lhf.hotel.common.exception.BusinessException;
import com.lhf.hotel.common.enums.RoomStatus;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.room.mapper.CleaningTaskMapper;
import com.lhf.hotel.room.mapper.MaintenanceRecordMapper;
import com.lhf.hotel.room.mapper.RoomMapper;
import com.lhf.hotel.room.mapper.RoomTypeMapper;
import com.lhf.hotel.room.model.dto.RoomSaveDTO;
import com.lhf.hotel.room.model.entity.CleaningTask;
import com.lhf.hotel.room.model.entity.MaintenanceRecord;
import com.lhf.hotel.room.model.entity.Room;
import com.lhf.hotel.room.model.entity.RoomType;
import com.lhf.hotel.room.model.vo.DashboardVO;
import com.lhf.hotel.room.model.vo.RoomVO;
import com.lhf.hotel.room.model.vo.TaskInfo;
import com.lhf.hotel.room.service.RoomService;
import com.lhf.hotel.room.support.RoomConverters;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final CleaningTaskMapper cleaningTaskMapper;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    public RoomServiceImpl(RoomMapper roomMapper, RoomTypeMapper roomTypeMapper,
                           CleaningTaskMapper cleaningTaskMapper,
                           MaintenanceRecordMapper maintenanceRecordMapper) {
        this.roomMapper = roomMapper;
        this.roomTypeMapper = roomTypeMapper;
        this.cleaningTaskMapper = cleaningTaskMapper;
        this.maintenanceRecordMapper = maintenanceRecordMapper;
    }

    @Override
    public PageResult<RoomVO> list(int page, int size, Long roomTypeId, String status,
                                   Integer floor, String keyword) {
        return queryRooms(page, size, roomTypeId, status, floor, keyword, isAuthenticated());
    }

    @Override
    public RoomVO getById(Long id) {
        Room room = roomMapper.selectById(id);
        Assert.notNull(room, "房间不存在");
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        RoomVO vo = RoomConverters.toRoomVO(room, roomType);
        vo.setCurrentTask(resolveCurrentTask(room));
        return vo;
    }

    @Override
    public Long create(RoomSaveDTO dto) {
        long exists = roomMapper.selectCount(
                new LambdaQueryWrapper<Room>().eq(Room::getRoomNumber, dto.getRoomNumber()));
        Assert.isTrue(exists == 0, "房间编号已存在");
        RoomType roomType = roomTypeMapper.selectById(dto.getRoomTypeId());
        Assert.notNull(roomType, "房型不存在");

        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomTypeId(dto.getRoomTypeId());
        room.setFloor(dto.getFloor());
        room.setDescription(dto.getDescription());
        room.setPrice(dto.getPrice() != null ? dto.getPrice() : roomType.getPrice());
        room.setStatus(RoomStatus.空闲中.name());
        roomMapper.insert(room);
        return room.getId();
    }

    @Override
    public void update(Long id, RoomSaveDTO dto) {
        Room room = roomMapper.selectById(id);
        Assert.notNull(room, "房间不存在");
        if (dto.getRoomNumber() != null && !dto.getRoomNumber().equals(room.getRoomNumber())) {
            long exists = roomMapper.selectCount(
                    new LambdaQueryWrapper<Room>()
                            .eq(Room::getRoomNumber, dto.getRoomNumber())
                            .ne(Room::getId, id));
            Assert.isTrue(exists == 0, "房间编号已存在");
            room.setRoomNumber(dto.getRoomNumber());
        }
        if (dto.getRoomTypeId() != null) {
            RoomType roomType = roomTypeMapper.selectById(dto.getRoomTypeId());
            Assert.notNull(roomType, "房型不存在");
            room.setRoomTypeId(dto.getRoomTypeId());
            if (dto.getPrice() == null) {
                room.setPrice(roomType.getPrice());
            }
        }
        if (dto.getPrice() != null) room.setPrice(dto.getPrice());
        if (dto.getFloor() != null) room.setFloor(dto.getFloor());
        if (dto.getDescription() != null) room.setDescription(dto.getDescription());
        roomMapper.updateById(room);
    }

    @Override
    public void delete(Long id) {
        Room room = roomMapper.selectById(id);
        Assert.notNull(room, "房间不存在");
        Assert.isTrue(RoomStatus.空闲中.name().equals(room.getStatus())
                        || RoomStatus.维修中.name().equals(room.getStatus()),
                "房间当前状态不允许删除");
        roomMapper.deleteById(id);
    }

    @Override
    public DashboardVO dashboard(Integer floor) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (floor != null) {
            wrapper.eq(Room::getFloor, floor);
        }
        wrapper.orderByAsc(Room::getFloor).orderByAsc(Room::getRoomNumber);
        List<Room> rooms = roomMapper.selectList(wrapper);
        Map<Long, RoomType> typeMap = loadRoomTypeMap(rooms);

        Map<String, Integer> summary = new LinkedHashMap<>();
        for (RoomStatus s : RoomStatus.values()) {
            summary.put(s.name(), 0);
        }
        List<RoomVO> roomVOs = rooms.stream().map(room -> {
            summary.merge(room.getStatus(), 1, Integer::sum);
            RoomType type = typeMap.get(room.getRoomTypeId());
            RoomVO vo = RoomConverters.toRoomVO(room, type);
            if (RoomStatus.打扫中.name().equals(room.getStatus())) {
                CleaningTask task = findActiveCleaningTask(room.getId());
                if (task != null) {
                    vo.setCleanerName(task.getCleanerName());
                    vo.setTaskStartTime(task.getStartTime());
                }
            }
            return vo;
        }).toList();

        return DashboardVO.builder()
                .total(rooms.size())
                .summary(summary)
                .rooms(roomVOs)
                .build();
    }

    @Override
    public PageResult<RoomVO> listByStatus(String status, int page, int size) {
        Assert.isTrue(isValidStatus(status), "无效的房间状态");
        return queryRooms(page, size, null, status, null, null, true);
    }

    @Override
    public void changeStatus(Long id, RoomStatusChangeDTO dto) {
        Room room = roomMapper.selectById(id);
        Assert.notNull(room, "房间不存在");

        RoomStatus current = RoomStatus.valueOf(room.getStatus());
        RoomStatus target = RoomStatus.valueOf(dto.getStatus());
        Assert.isTrue(current.canTransitionTo(target),
                "非法状态跳转: " + current.name() + " → " + target.name());

        if (target == RoomStatus.维修中) {
            Assert.hasText(dto.getReason(), "维修原因不能为空");
            createMaintenanceRecord(room, dto.getReason());
        } else if (current == RoomStatus.维修中 && target == RoomStatus.空闲中) {
            completeMaintenanceRecord(room.getId(), null);
        }

        room.setStatus(target.name());
        int affected = roomMapper.updateById(room);
        if (affected == 0) {
            throw new BusinessException(409, "房间 " + room.getRoomNumber() + " 状态已被其他请求修改，请重试");
        }
    }

    private PageResult<RoomVO> queryRooms(int page, int size, Long roomTypeId, String status,
                                          Integer floor, String keyword, boolean authenticated) {
        LambdaQueryWrapper<Room> wrapper = buildRoomQuery(roomTypeId, status, floor, keyword);
        if (!authenticated) {
            wrapper.eq(Room::getStatus, RoomStatus.空闲中.name());
        }
        wrapper.orderByAsc(Room::getFloor).orderByAsc(Room::getRoomNumber);

        Page<Room> result = roomMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, RoomType> typeMap = loadRoomTypeMap(result.getRecords());
        List<RoomVO> records = result.getRecords().stream()
                .map(r -> RoomConverters.toRoomVO(r, typeMap.get(r.getRoomTypeId())))
                .toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    private LambdaQueryWrapper<Room> buildRoomQuery(Long roomTypeId, String status,
                                                  Integer floor, String keyword) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (roomTypeId != null) wrapper.eq(Room::getRoomTypeId, roomTypeId);
        if (StringUtils.hasText(status)) wrapper.eq(Room::getStatus, status);
        if (floor != null) wrapper.eq(Room::getFloor, floor);
        if (StringUtils.hasText(keyword)) wrapper.like(Room::getRoomNumber, keyword);
        return wrapper;
    }

    private boolean isAuthenticated() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return false;
        }
        HttpServletRequest request = attrs.getRequest();
        if (StringUtils.hasText(request.getHeader("X-UserId"))) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        return StringUtils.hasText(auth) && auth.startsWith("Bearer ");
    }

    private Map<Long, RoomType> loadRoomTypeMap(List<Room> rooms) {
        List<Long> typeIds = rooms.stream().map(Room::getRoomTypeId).distinct().toList();
        if (typeIds.isEmpty()) {
            return Map.of();
        }
        return roomTypeMapper.selectBatchIds(typeIds).stream()
                .collect(Collectors.toMap(RoomType::getId, Function.identity()));
    }

    private TaskInfo resolveCurrentTask(Room room) {
        if (RoomStatus.打扫中.name().equals(room.getStatus())) {
            CleaningTask task = findActiveCleaningTask(room.getId());
            return task != null ? RoomConverters.toCleaningTaskInfo(task) : null;
        }
        if (RoomStatus.维修中.name().equals(room.getStatus())) {
            MaintenanceRecord record = findActiveMaintenanceRecord(room.getId());
            return record != null ? RoomConverters.toMaintenanceTaskInfo(record) : null;
        }
        return null;
    }

    private CleaningTask findActiveCleaningTask(Long roomId) {
        return cleaningTaskMapper.selectOne(
                new LambdaQueryWrapper<CleaningTask>()
                        .eq(CleaningTask::getRoomId, roomId)
                        .eq(CleaningTask::getStatus, "打扫中")
                        .orderByDesc(CleaningTask::getStartTime)
                        .last("LIMIT 1"));
    }

    private MaintenanceRecord findActiveMaintenanceRecord(Long roomId) {
        return maintenanceRecordMapper.selectOne(
                new LambdaQueryWrapper<MaintenanceRecord>()
                        .eq(MaintenanceRecord::getRoomId, roomId)
                        .eq(MaintenanceRecord::getStatus, "维修中")
                        .orderByDesc(MaintenanceRecord::getStartTime)
                        .last("LIMIT 1"));
    }

    private void createMaintenanceRecord(Room room, String reason) {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setRoomId(room.getId());
        record.setRoomNumber(room.getRoomNumber());
        record.setReason(reason);
        record.setStatus("维修中");
        record.setStartTime(LocalDateTime.now());
        record.setCost(BigDecimal.ZERO);
        maintenanceRecordMapper.insert(record);
    }

    private void completeMaintenanceRecord(Long roomId, BigDecimal cost) {
        MaintenanceRecord record = findActiveMaintenanceRecord(roomId);
        if (record == null) {
            return;
        }
        record.setStatus("已完成");
        record.setEndTime(LocalDateTime.now());
        if (cost != null) {
            record.setCost(cost);
        }
        maintenanceRecordMapper.updateById(record);
    }

    private boolean isValidStatus(String status) {
        try {
            RoomStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
