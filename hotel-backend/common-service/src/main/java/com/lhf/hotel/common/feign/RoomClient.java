package com.lhf.hotel.common.feign;

import com.lhf.hotel.common.dto.CleaningTaskDTO;
import com.lhf.hotel.common.dto.RoomStatusChangeDTO;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * room-service Feign 接口 —— 供 order-service / user-service 调用
 */
@FeignClient(name = "room-service", contextId = "commonRoomClient", path = "/api/room")
public interface RoomClient {

    @PutMapping("/{id}/status")
    Result<Void> changeStatus(@PathVariable Long id, @RequestBody RoomStatusChangeDTO dto);

    /** 打扫任务分页（按保洁员筛选） */
    @GetMapping("/cleaning/tasks")
    Result<PageResult<CleaningTaskDTO>> listCleaningTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long cleanerId,
            @RequestParam(required = false) String date);
}