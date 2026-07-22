package com.lhf.hotel.room.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lhf.hotel.common.dto.CleanerDTO;
import com.lhf.hotel.common.enums.RoomStatus;
import com.lhf.hotel.common.util.SchedulerLock;
import com.lhf.hotel.room.mapper.CleaningTaskMapper;
import com.lhf.hotel.room.mapper.RoomMapper;
import com.lhf.hotel.room.model.entity.CleaningTask;
import com.lhf.hotel.room.model.entity.Room;
import com.lhf.hotel.room.service.impl.CleaningServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 打扫定时调度 — 每 10 秒推进一次过期状态
 */
@Component
public class CleaningScheduler {

    private static final Logger log = LoggerFactory.getLogger(CleaningScheduler.class);
    private static final int PENDING_DELAY_MINUTES = 1;
    private static final int CLEANING_DURATION_MINUTES = 5;

    private final RoomMapper roomMapper;
    private final CleaningTaskMapper taskMapper;
    private final CleaningServiceImpl cleaningService;
    private final SchedulerLock schedulerLock;

    public CleaningScheduler(RoomMapper roomMapper, CleaningTaskMapper taskMapper,
                             CleaningServiceImpl cleaningService, SchedulerLock schedulerLock) {
        this.roomMapper = roomMapper;
        this.taskMapper = taskMapper;
        this.cleaningService = cleaningService;
        this.schedulerLock = schedulerLock;
    }

    @Scheduled(fixedDelay = 10_000)
    public void processCleaning() {
        String lockKey = "scheduler:cleaning";
        String owner = schedulerLock.tryLockWithOwner(lockKey, 60);
        if (owner == null) {
            return;
        }
        try {
            assignPendingRooms();
            completeExpiredCleaning();
        } finally {
            schedulerLock.unlock(lockKey, owner);
        }
    }

    /** 待清洁中超过 1 分钟 → 自动分配保洁 → 打扫中 */
    private void assignPendingRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(PENDING_DELAY_MINUTES);
        List<Room> pendingRooms = roomMapper.selectList(
                new LambdaQueryWrapper<Room>()
                        .eq(Room::getStatus, RoomStatus.待清洁中.name())
                        .lt(Room::getUpdateTime, threshold));

        if (pendingRooms.isEmpty()) {
            return;
        }

        List<CleanerDTO> cleaners = cleaningService.fetchCleaners();
        CleanerDTO cleaner = cleaningService.pickCleanerWithMinTasks(cleaners);
        if (cleaner == null) {
            log.warn("无可用保洁员，{} 间待清洁房间暂缓分配", pendingRooms.size());
            return;
        }

        for (Room room : pendingRooms) {
            CleaningTask task = new CleaningTask();
            task.setRoomId(room.getId());
            task.setRoomNumber(room.getRoomNumber());
            task.setCleanerId(cleaner.getId());
            task.setCleanerName(cleaner.getRealName());
            task.setStatus("打扫中");
            task.setStartTime(LocalDateTime.now());
            taskMapper.insert(task);

            room.setStatus(RoomStatus.打扫中.name());
            roomMapper.updateById(room);
            log.info("自动分配保洁 {} → 房间 {}", cleaner.getRealName(), room.getRoomNumber());
        }
    }

    /** 打扫中超过 5 分钟 → 空闲中，任务标记已完成 */
    private void completeExpiredCleaning() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(CLEANING_DURATION_MINUTES);
        List<CleaningTask> expiredTasks = taskMapper.selectList(
                new LambdaQueryWrapper<CleaningTask>()
                        .eq(CleaningTask::getStatus, "打扫中")
                        .lt(CleaningTask::getStartTime, threshold));

        for (CleaningTask task : expiredTasks) {
            task.setStatus("已完成");
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);

            Room room = roomMapper.selectById(task.getRoomId());
            if (room != null && RoomStatus.打扫中.name().equals(room.getStatus())) {
                room.setStatus(RoomStatus.空闲中.name());
                roomMapper.updateById(room);
                log.info("房间 {} 打扫完成，恢复空闲", room.getRoomNumber());
            }
        }
    }
}
