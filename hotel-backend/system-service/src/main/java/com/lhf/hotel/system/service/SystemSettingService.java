package com.lhf.hotel.system.service;

import com.lhf.hotel.system.model.dto.SettingUpdateDTO;
import com.lhf.hotel.system.model.vo.SettingVO;

import java.util.List;

public interface SystemSettingService {

    /** 获取全部系统设置（表为空时自动 seed 默认值） */
    List<SettingVO> listAll();

    /** 批量更新系统设置（key 不存在则新增） */
    List<SettingVO> updateBatch(List<SettingUpdateDTO> items);
}
