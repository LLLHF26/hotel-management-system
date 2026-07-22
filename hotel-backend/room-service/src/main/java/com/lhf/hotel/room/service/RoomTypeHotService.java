package com.lhf.hotel.room.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.SchedulerLock;
import com.lhf.hotel.room.feign.order.OrderStatsFeignClient;
import com.lhf.hotel.room.mapper.RoomTypeMapper;
import com.lhf.hotel.room.model.entity.RoomType;
import com.lhf.hotel.room.model.vo.HotRoomTypeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomTypeHotService {

    private static final String CACHE_KEY = "hot:roomTypes";
    private static final long CACHE_TTL = 70; // 分钟，比定时任务周期略长

    private final RoomTypeMapper roomTypeMapper;
    private final OrderStatsFeignClient orderStatsFeignClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SchedulerLock schedulerLock;

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    public void refreshHotRoomTypes() {
        String lockKey = "scheduler:hotRoomType";
        String owner = schedulerLock.tryLockWithOwner(lockKey, 120);
        if (owner == null) {
            log.debug("热门房型刷新跳过（其他实例持有锁）");
            return;
        }
        try {
            Result<List<Map<String, Object>>> result = orderStatsFeignClient.getHotRoomTypes(6, 30);
            if (result == null || result.getData() == null) {
                log.warn("获取热门房型统计失败：order-service 返回为空");
                return;
            }
            List<Map<String, Object>> counts = result.getData();
            log.info("Order stats returned {} room types", counts.size());
            List<HotRoomTypeVO> hotList = new ArrayList<>();

            for (Map<String, Object> row : counts) {
                String roomTypeName = (String) row.get("roomTypeName");
                Long orderCount = ((Number) row.get("orderCount")).longValue();

                RoomType entity = roomTypeMapper.selectOne(
                        new LambdaQueryWrapper<RoomType>().eq(RoomType::getName, roomTypeName));
                if (entity == null) continue;

                hotList.add(HotRoomTypeVO.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .bedType(entity.getBedType())
                        .maxGuests(entity.getMaxGuests())
                        .price(entity.getPrice())
                        .coverImage(entity.getCoverImage())
                        .description(entity.getDescription())
                        .orderCount(orderCount)
                        .build());
            }

            String json = objectMapper.writeValueAsString(hotList);
            redisTemplate.opsForValue().set(CACHE_KEY, json, CACHE_TTL, TimeUnit.MINUTES);
            log.info("热门房型缓存已刷新，共 {} 条", hotList.size());
        } catch (Exception e) {
            log.warn("刷新热门房型缓存失败（order-service 不可用），将沿用旧缓存", e.toString());
        } finally {
            schedulerLock.unlock(lockKey, owner);
        }
    }

    public List<HotRoomTypeVO> getHotRoomTypes() {
        // 1) 优先读缓存（含订单统计排序）
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY);
            if (json != null && !json.isEmpty()) {
                List<HotRoomTypeVO> cached = objectMapper.readValue(json, new TypeReference<>() {});
                if (cached != null && !cached.isEmpty()) {
                    return cached;
                }
            }
        } catch (Exception e) {
            log.warn("读取热门房型缓存失败，降级到数据库兜底", e.toString());
        }

        // 2) 缓存为空 → 从数据库取所有未删除的房型作为兜底（按排序字段）
        log.info("热门房型缓存为空，使用数据库兜底");
        List<RoomType> entities = roomTypeMapper.selectList(
                new LambdaQueryWrapper<RoomType>()
                        .eq(RoomType::getIsDeleted, 0)
                        .orderByAsc(RoomType::getSortOrder));
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<HotRoomTypeVO> fallback = new ArrayList<>();
        for (RoomType rt : entities) {
            fallback.add(HotRoomTypeVO.builder()
                    .id(rt.getId())
                    .name(rt.getName())
                    .bedType(rt.getBedType())
                    .maxGuests(rt.getMaxGuests())
                    .price(rt.getPrice())
                    .coverImage(rt.getCoverImage())
                    .description(rt.getDescription())
                    .orderCount(0L)
                    .build());
        }
        return fallback;
    }
}
