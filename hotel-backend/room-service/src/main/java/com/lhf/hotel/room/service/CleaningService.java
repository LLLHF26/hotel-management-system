package com.lhf.hotel.room.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.room.model.dto.CleaningAssignDTO;
import com.lhf.hotel.room.model.vo.CleaningAssignResult;
import com.lhf.hotel.room.model.vo.CleaningTaskVO;

import java.util.List;

public interface CleaningService {

    PageResult<CleaningTaskVO> listTasks(int page, int size, String status, Long cleanerId, String date);

    CleaningTaskVO getTaskById(Long id);

    CleaningAssignResult assign(Long roomId, CleaningAssignDTO dto);

    List<CleaningTaskVO> getActiveTasksByCleaner(Long cleanerId);
}
