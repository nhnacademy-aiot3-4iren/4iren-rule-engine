package com.nhnacademy.ruleengine.common.cache.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomDeviceCacheRepository {
    private static final String KEY_PREFIX = "room:%d:devices";
    private static final Duration TTL = Duration.ofMinutes(10);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private String getKey(Long roomId){
        return KEY_PREFIX.formatted(roomId);
    }

    public List<ExternalRoomDeviceInfo> get(Long roomId) {
        Object cached = redisTemplate.opsForValue().get(getKey(roomId));
        if (cached == null) return null;
        return objectMapper.convertValue(cached, new TypeReference<List<ExternalRoomDeviceInfo>>() {});
    }

    public void set(Long roomId, List<ExternalRoomDeviceInfo> devices) {
        redisTemplate.opsForValue().set(getKey(roomId), devices, TTL);
    }
}
