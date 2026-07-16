package com.lhf.hotel.room.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.room.model.dto.RoomTypeSaveDTO;
import com.lhf.hotel.room.model.vo.RoomTypeVO;

public interface RoomTypeService {

    PageResult<RoomTypeVO> list(int page, int size, String keyword, String bedType);

    RoomTypeVO getById(Long id);

    Long create(RoomTypeSaveDTO dto);

    void update(Long id, RoomTypeSaveDTO dto);

    void delete(Long id);
}
