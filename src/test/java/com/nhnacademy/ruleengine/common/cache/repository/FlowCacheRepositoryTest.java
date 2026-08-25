package com.nhnacademy.ruleengine.common.cache.repository;

import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowCacheRepositoryTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private FlowCacheRepository repository;

    private final Long roomId = 101L;
    private final String expectedKey = "flow:room:101";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("캐시 조회 시 데이터가 없으면 null 반환")
    void get_CacheMiss() {
        when(valueOperations.get(expectedKey)).thenReturn(null);

        List<ExecutableFlow> result = repository.get(roomId);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("캐시 조회 시 데이터가 있으면 List<ExecutableFlow>로 캐스팅하여 반환")
    void get_CacheHit() {
        List<ExecutableFlow> expectedList = List.of(); // 더미 리스트
        when(valueOperations.get(expectedKey)).thenReturn(expectedList);

        List<ExecutableFlow> result = repository.get(roomId);

        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    @DisplayName("Redis에 Flow 리스트를 TTL과 함께 저장")
    void set_Success() {
        List<ExecutableFlow> flows = List.of();

        repository.set(roomId, flows);

        verify(valueOperations).set(eq(expectedKey), eq(flows), any(Duration.class));
    }

    @Test
    @DisplayName("evict 호출 시 Redis에서 해당 키 삭제")
    void evict_Success() {
        repository.evict(roomId);

        verify(redisTemplate).delete(expectedKey);
    }
}