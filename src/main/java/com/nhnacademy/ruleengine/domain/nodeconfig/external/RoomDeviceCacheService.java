package com.nhnacademy.ruleengine.domain.nodeconfig.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomDeviceCacheService {
    private static final Duration TTL = Duration.ofMinutes(10);

    @Qualifier("externalRoomDeviceRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoomDeviceClient roomDeviceClient;

    public List<ExternalRoomDeviceInfo> getRoomDevices(Long roomId){
        String key = buildKey(roomId);

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List<?> cachedList) {
            @SuppressWarnings("unchecked")
            List<ExternalRoomDeviceInfo> devices = (List<ExternalRoomDeviceInfo>) cachedList;
            log.debug("Redis cache hit. roomId={}, key={}", roomId, key);
            return devices;
        }

        List<ExternalRoomDeviceInfo> devices = roomDeviceClient.getRoomDevices(roomId);

        if (devices == null) {
            devices = List.of();
        }
        redisTemplate.opsForValue().set(key, devices, TTL);
        return devices;
    }

    public String buildKey(Long roomId){
        return "room:%d:devices".formatted(roomId);
    }
}
//TODO @Cacheable, Redis cashe lock 등 알아보기