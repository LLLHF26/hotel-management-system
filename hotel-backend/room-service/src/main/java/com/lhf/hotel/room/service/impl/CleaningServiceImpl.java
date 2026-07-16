package com.lhf.hotel.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.asserts.Assert;
import com.lhf.hotel.common.dto.CleanerDTO;
import com.lhf.hotel.common.feign.UserClient;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.room.mapper.CleaningTaskMapper;
import com.lhf.hotel.room.mapper.RoomMapper;
import com.lhf.hotel.room.mapper.RoomTypeMapper;
import com.lhf.hotel.room.model.dto.CleaningAssignDTO;
import com.lhf.hotel.room.model.entity.CleaningTask;
import com.lhf.hotel.room.model.entity.Room;
import com.lhf.hotel.room.model.entity.RoomType;
import com.lhf.hotel.room.model.vo.CleaningAssignResult;
import com.lhf.hotel.room.model.vo.CleaningTaskVO;
import com.lhf.hotel.room.service.CleaningService;
import com.lhf.hotel.room.support.RoomConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CleaningServiceImpl implements CleaningService {

    private static final Logger log = LoggerFactory.getLogger(CleaningServiceImpl.class);

    private final CleaningTaskMapper taskMapper;
    private final RoomMapper roomMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final UserClient userClient;

    public CleaningServiceImpl(CleaningTaskMapper taskMapper, RoomMapper roomMapper,
                               RoomTypeMapper roomTypeMapper, UserClient userClient) {
        this.taskMapper = taskMapper;
        this.roomMapper = roomMapper;
        this.roomTypeMapper = roomTypeMapper;
        this.userClient = userClient;
    }

    @Override
    public PageResult<CleaningTaskVO> listTasks(int page, int size, String status,
                                                Long cleanerId, String date) {
        LambdaQueryWrapper<CleaningTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(CleaningTask::getStatus, status);
        }
        if (cleanerId != null) {
            wrapper.eq(CleaningTask::getCleanerId, cleanerId);
        }
        if (StringUtils.hasText(date)) {
            LocalDate day = LocalDate.parse(date);
            wrapper.ge(CleaningTask::getStartTime, day.atStartOfDay())
                    .lt(CleaningTask::getStartTime, day.plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(CleaningTask::getStartTime);

        Page<CleaningTask> result = taskMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, RoomType> typeMap = loadRoomTypeMapForTasks(result.getRecords());
        List<CleaningTaskVO> records = result.getRecords().stream()
                .map(t -> {
                    Room room = roomMapper.selectById(t.getRoomId());
                    RoomType type = room != null ? typeMap.get(room.getRoomTypeId()) : null;
                    return RoomConverters.toCleaningTaskVO(t, type != null ? type.getName() : null);
                })
                .toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public CleaningTaskVO getTaskById(Long id) {
        CleaningTask task = taskMapper.selectById(id);
        Assert.notNull(task, "打扫任务不存在");
        Room room = roomMapper.selectById(task.getRoomId());
        String roomTypeName = null;
        if (room != null) {
            RoomType type = roomTypeMapper.selectById(room.getRoomTypeId());
            roomTypeName = type != null ? type.getName() : null;
        }
        return RoomConverters.toCleaningTaskVO(task, roomTypeName);
    }

    @Override
    public CleaningAssignResult assign(Long roomId, CleaningAssignDTO dto) {
        Room room = roomMapper.selectById(roomId);
        Assert.notNull(room, "房间不存在");
        Assert.isTrue("待清洁中".equals(room.getStatus()), "房间不是待清洁中状态");

        String cleanerName = resolveCleanerName(dto.getCleanerId());

        CleaningTask task = new CleaningTask();
        task.setRoomId(roomId);
        task.setRoomNumber(room.getRoomNumber());
        task.setCleanerId(dto.getCleanerId());
        task.setCleanerName(cleanerName);
        task.setStatus("打扫中");
        task.setStartTime(LocalDateTime.now());
        taskMapper.insert(task);

        room.setStatus("打扫中");
        roomMapper.updateById(room);

        return new CleaningAssignResult(task.getId(), cleanerName);
    }

    @Override
    public List<CleaningTaskVO> getActiveTasksByCleaner(Long cleanerId) {
        List<CleaningTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<CleaningTask>()
                        .eq(CleaningTask::getCleanerId, cleanerId)
                        .eq(CleaningTask::getStatus, "打扫中")
                        .orderByDesc(CleaningTask::getStartTime));
        return tasks.stream()
                .map(t -> RoomConverters.toCleaningTaskVO(t, null))
                .toList();
    }

    /** 定时调度：选任务最少的保洁员 */
    public CleanerDTO pickCleanerWithMinTasks(List<CleanerDTO> cleaners) {
        if (cleaners == null || cleaners.isEmpty()) {
            return null;
        }
        CleanerDTO selected = null;
        long minCount = Long.MAX_VALUE;
        for (CleanerDTO cleaner : cleaners) {
            long count = taskMapper.selectCount(
                    new LambdaQueryWrapper<CleaningTask>()
                            .eq(CleaningTask::getCleanerId, cleaner.getId())
                            .eq(CleaningTask::getStatus, "打扫中"));
            if (count < minCount) {
                minCount = count;
                selected = cleaner;
            }
        }
        return selected;
    }

    public List<CleanerDTO> fetchCleaners() {
        try {
            Result<List<CleanerDTO>> result = userClient.getCleanerList();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("获取保洁员列表失败: {}", e.getMessage());
        }
        return List.of();
    }

    private String resolveCleanerName(Long cleanerId) {
        return fetchCleaners().stream()
                .filter(c -> c.getId().equals(cleanerId))
                .map(CleanerDTO::getRealName)
                .findFirst()
                .orElse("保洁员" + cleanerId);
    }

    private Map<Long, RoomType> loadRoomTypeMapForTasks(List<CleaningTask> tasks) {
        List<Long> roomIds = tasks.stream().map(CleaningTask::getRoomId).distinct().toList();
        if (roomIds.isEmpty()) {
            return Map.of();
        }
        List<Room> rooms = roomMapper.selectBatchIds(roomIds);
        List<Long> typeIds = rooms.stream().map(Room::getRoomTypeId).distinct().toList();
        if (typeIds.isEmpty()) {
            return Map.of();
        }
        return roomTypeMapper.selectBatchIds(typeIds).stream()
                .collect(Collectors.toMap(RoomType::getId, Function.identity()));
    }

}
