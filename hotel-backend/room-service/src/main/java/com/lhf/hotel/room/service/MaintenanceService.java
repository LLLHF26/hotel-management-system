package com.lhf.hotel.room.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.room.model.dto.MaintenanceCompleteDTO;
import com.lhf.hotel.room.model.dto.MaintenanceStartDTO;
import com.lhf.hotel.room.model.vo.MaintenanceRecordVO;

public interface MaintenanceService {

    Long start(Long roomId, MaintenanceStartDTO dto);

    void complete(Long roomId, MaintenanceCompleteDTO dto);

    PageResult<MaintenanceRecordVO> records(int page, int size, Long roomId, String status,
                                            String startDate, String endDate);
}
