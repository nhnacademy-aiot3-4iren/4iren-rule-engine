package com.nhnacademy.ruleengine.common.cache.repository;

import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FlowCacheRepository {
    private static final String KEY_PREFIX = "flow:room:";
    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, Object> redisTemplate;

    private String getKey(Long roomId){
        return KEY_PREFIX.formatted(roomId);
    }


    @SuppressWarnings("unchecked")
    public List<ExecutableFlow> get(Long roomId) {
        Object cached = redisTemplate.opsForValue().get(getKey(roomId));
        if (cached == null) return null;
        return (List<ExecutableFlow>) cached;
    }

    public void set(Long roomId, List<ExecutableFlow> flows) {
        redisTemplate.opsForValue().set(getKey(roomId), flows, TTL);
    }

    public void evict(Long roomId) {
        redisTemplate.delete(getKey(roomId));
    }
}
