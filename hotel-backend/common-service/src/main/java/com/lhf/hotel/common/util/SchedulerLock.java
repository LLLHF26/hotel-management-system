package com.lhf.hotel.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SchedulerLock {

    private final StringRedisTemplate redisTemplate;

    public SchedulerLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key, long ttlSeconds) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", ttlSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取调度锁成功: {}", key);
            return true;
        }
        log.debug("获取调度锁失败（其他实例持有）: {}", key);
        return false;
    }

    public String tryLockWithOwner(String key, long ttlSeconds) {
        String owner = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, owner, ttlSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取调度锁成功: {} (owner={})", key, owner);
            return owner;
        }
        log.debug("获取调度锁失败（其他实例持有）: {}", key);
        return null;
    }

    public void unlock(String key, String owner) {
        if (owner == null) return;
        String current = redisTemplate.opsForValue().get(key);
        if (owner.equals(current)) {
            redisTemplate.delete(key);
            log.debug("释放调度锁: {}", key);
        }
    }
}
