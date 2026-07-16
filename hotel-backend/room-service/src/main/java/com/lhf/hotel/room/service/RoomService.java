package com.lhf.hotel.room.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.room.model.dto.RoomSaveDTO;
import com.lhf.hotel.common.dto.RoomStatusChangeDTO;
import com.lhf.hotel.room.model.vo.DashboardVO;
import com.lhf.hotel.room.model.vo.RoomVO;

public interface RoomService {

    // ===== 房间 CRUD =====
    PageResult<RoomVO> list(int page, int size, Long roomTypeId, String status, Integer floor, String keyword);

    RoomVO getById(Long id);

    Long create(RoomSaveDTO dto);

    void update(Long id, RoomSaveDTO dto);

    void delete(Long id);

    // ===== 房态 =====
    DashboardVO dashboard(Integer floor);

    PageResult<RoomVO> listByStatus(String status, int page, int size);

    void changeStatus(Long id, RoomStatusChangeDTO dto);
}
