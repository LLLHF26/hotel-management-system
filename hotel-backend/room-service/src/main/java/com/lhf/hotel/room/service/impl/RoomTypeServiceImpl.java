package com.lhf.hotel.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.asserts.Assert;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.room.mapper.RoomMapper;
import com.lhf.hotel.room.mapper.RoomTypeMapper;
import com.lhf.hotel.room.model.dto.RoomTypeSaveDTO;
import com.lhf.hotel.room.model.entity.Room;
import com.lhf.hotel.room.model.entity.RoomType;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import com.lhf.hotel.room.service.RoomTypeService;
import com.lhf.hotel.room.support.RoomConverters;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeMapper mapper;
    private final RoomMapper roomMapper;

    public RoomTypeServiceImpl(RoomTypeMapper mapper, RoomMapper roomMapper) {
        this.mapper = mapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public PageResult<RoomTypeVO> list(int page, int size, String keyword, String bedType) {
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(RoomType::getName, keyword);
        }
        if (StringUtils.hasText(bedType)) {
            wrapper.like(RoomType::getBedType, bedType);
        }
        wrapper.orderByAsc(RoomType::getSortOrder).orderByAsc(RoomType::getId);

        Page<RoomType> result = mapper.selectPage(new Page<>(page, size), wrapper);
        List<RoomTypeVO> records = result.getRecords().stream()
                .map(e -> RoomConverters.toRoomTypeVO(e, false))
                .toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public RoomTypeVO getById(Long id) {
        RoomType entity = mapper.selectById(id);
        Assert.notNull(entity, "房型不存在");
        return RoomConverters.toRoomTypeVO(entity, true);
    }

    @Override
    public Long create(RoomTypeSaveDTO dto) {
        RoomType entity = new RoomType();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
        entity.setArea(dto.getArea());
        entity.setBedType(dto.getBedType());
        entity.setMaxGuests(dto.getMaxGuests() != null ? dto.getMaxGuests() : 2);
        entity.setCoverImage(dto.getCoverImage());
        entity.setImages(RoomConverters.serializeImages(dto.getImages()));
        entity.setAmenities(dto.getAmenities());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, RoomTypeSaveDTO dto) {
        RoomType entity = mapper.selectById(id);
        Assert.notNull(entity, "房型不存在");
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getArea() != null) entity.setArea(dto.getArea());
        if (dto.getBedType() != null) entity.setBedType(dto.getBedType());
        if (dto.getMaxGuests() != null) entity.setMaxGuests(dto.getMaxGuests());
        if (dto.getCoverImage() != null) entity.setCoverImage(dto.getCoverImage());
        if (dto.getImages() != null) entity.setImages(RoomConverters.serializeImages(dto.getImages()));
        if (dto.getAmenities() != null) entity.setAmenities(dto.getAmenities());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        Assert.notNull(mapper.selectById(id), "房型不存在");
        long roomCount = roomMapper.selectCount(
                new LambdaQueryWrapper<Room>().eq(Room::getRoomTypeId, id));
        Assert.isTrue(roomCount == 0, "该房型下还有房间，无法删除");
        mapper.deleteById(id);
    }
}
