package com.nhnacademy.ruleengine.common.cache.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomDeviceCacheRepositoryTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private RoomDeviceCacheRepository repository;

    private final Long roomId = 101L;
    private final String expectedKey = "room:101:devices";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("캐시 조회 시 데이터가 없으면 null 반환")
    void get_CacheMiss() {
        when(valueOperations.get(expectedKey)).thenReturn(null);

        List<ExternalRoomDeviceInfo> result = repository.get(roomId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("캐시 조회 시 데이터가 있으면 ObjectMapper를 통해 변환 후 반환")
    void get_CacheHit() {
        Object cachedData = new Object();
        List<ExternalRoomDeviceInfo> expectedList = List.of(new ExternalRoomDeviceInfo(roomId, "eui", "name", null));

        when(valueOperations.get(expectedKey)).thenReturn(cachedData);
        when(objectMapper.convertValue(eq(cachedData), any(TypeReference.class))).thenReturn(expectedList);

        List<ExternalRoomDeviceInfo> result = repository.get(roomId);

        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    @DisplayName("Redis에 디바이스 리스트를 TTL과 함께 저장")
    void set_Success() {
        List<ExternalRoomDeviceInfo> devices = List.of(new ExternalRoomDeviceInfo(roomId, "eui", "name", null));

        repository.set(roomId, devices);

        verify(valueOperations).set(eq(expectedKey), eq(devices), any(Duration.class));
    }
}