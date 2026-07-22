package com.lhf.hotel.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lhf.hotel.system.mapper.SystemSettingMapper;
import com.lhf.hotel.system.model.dto.SettingUpdateDTO;
import com.lhf.hotel.system.model.entity.SystemSetting;
import com.lhf.hotel.system.model.vo.SettingVO;
import com.lhf.hotel.system.service.SystemSettingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingMapper settingMapper;

    /** 默认配置：表为空时 seed，保证非空且前端首屏有值 */
    private static final Map<String, String[]> DEFAULTS = new LinkedHashMap<>() {{
        put("hotelName",          new String[]{"云端酒店",                  "酒店名称"});
        put("phone",              new String[]{"010-88882222",              "联系电话"});
        put("address",            new String[]{"北京市朝阳区示例路 88 号",    "酒店地址"});
        put("checkInTime",        new String[]{"14:00",                     "入住时间"});
        put("checkOutTime",       new String[]{"12:00",                     "退房时间"});
        put("fullHouseThreshold", new String[]{"90%",                       "满房预警阈值"});
    }};

    public SystemSettingServiceImpl(SystemSettingMapper settingMapper) {
        this.settingMapper = settingMapper;
    }

    @Override
    public List<SettingVO> listAll() {
        seedIfEmpty();
        List<SystemSetting> rows = settingMapper.selectList(
                new LambdaQueryWrapper<SystemSetting>().orderByAsc(SystemSetting::getKey)
        );
        return rows.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SettingVO> updateBatch(List<SettingUpdateDTO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        for (SettingUpdateDTO item : items) {
            SystemSetting existing = settingMapper.selectById(item.getKey());
            if (existing == null) {
                SystemSetting row = new SystemSetting();
                row.setKey(item.getKey());
                row.setValue(item.getValue());
                row.setDescription(item.getKey());
                settingMapper.insert(row);
            } else {
                existing.setValue(item.getValue());
                settingMapper.updateById(existing);
            }
        }
        // 返回更新后的全量列表，便于前端刷新
        List<SystemSetting> rows = settingMapper.selectList(
                new LambdaQueryWrapper<SystemSetting>().orderByAsc(SystemSetting::getKey)
        );
        return rows.stream().map(this::toVO).toList();
    }

    /** 表为空时写入默认值 */
    private void seedIfEmpty() {
        Long count = settingMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        DEFAULTS.forEach((key, arr) -> {
            SystemSetting row = new SystemSetting();
            row.setKey(key);
            row.setValue(arr[0]);
            row.setDescription(arr[1]);
            settingMapper.insert(row);
        });
    }

    private SettingVO toVO(SystemSetting entity) {
        SettingVO vo = new SettingVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
