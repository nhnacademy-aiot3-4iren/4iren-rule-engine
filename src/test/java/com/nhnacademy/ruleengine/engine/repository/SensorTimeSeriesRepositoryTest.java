package com.nhnacademy.ruleengine.engine.repository;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorTimeSeriesRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private SensorTimeSeriesRepository repository;

    private static final Long ROOM_ID = 100L;
    private static final String EXPECTED_KEY = "room:100:metric:TEMPERATURE:ts";

    @BeforeEach
    void setUp() {
        repository = new SensorTimeSeriesRepository(redisTemplate);
    }

    @Test
    @DisplayName("save는 room/metric 키 규칙에 맞춰 ZADD, 오래된 데이터 정리, TTL 갱신을 모두 수행한다")
    void save_addsAndTrimsAndRefreshesExpiry() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Instant timestamp = Instant.parse("2026-08-18T00:00:00Z");

        repository.save(ROOM_ID, MeasurementType.TEMPERATURE, 26.5, timestamp);

        ArgumentCaptor<String> memberCaptor = ArgumentCaptor.forClass(String.class);
        verify(zSetOperations).add(eq(EXPECTED_KEY), memberCaptor.capture(), eq((double) timestamp.toEpochMilli()));
        assertThat(memberCaptor.getValue()).matches(timestamp.toEpochMilli() + ":26\\.5:-?\\d+");

        long expectedThreshold = timestamp.minus(SensorTimeSeriesRepository.MAX_RETENTION).toEpochMilli();
        verify(zSetOperations).removeRangeByScore(EXPECTED_KEY, 0, expectedThreshold);

        verify(redisTemplate).expire(EXPECTED_KEY, SensorTimeSeriesRepository.MAX_RETENTION);
    }

    @Test
    @DisplayName("save는 같은 timestamp/value가 연속으로 들어와도 member가 유일해지도록 nonce를 붙인다")
    void save_generatesUniqueMemberForIdenticalTimestampAndValue() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Instant timestamp = Instant.parse("2026-08-18T00:00:00Z");

        repository.save(ROOM_ID, MeasurementType.TEMPERATURE, 26.5, timestamp);
        repository.save(ROOM_ID, MeasurementType.TEMPERATURE, 26.5, timestamp);

        ArgumentCaptor<String> memberCaptor = ArgumentCaptor.forClass(String.class);
        verify(zSetOperations, times(2))
                .add(eq(EXPECTED_KEY), memberCaptor.capture(), eq((double) timestamp.toEpochMilli()));

        List<String> members = memberCaptor.getAllValues();
        assertThat(members.get(0)).isNotEqualTo(members.get(1));
    }

    @Test
    @DisplayName("getRange는 조회된 데이터를 시간순으로 정렬해서 반환한다")
    void getRange_returnsPointsSortedByTimestamp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Instant from = Instant.ofEpochMilli(1_000);
        Instant to = Instant.ofEpochMilli(3_000);

        // 순서를 일부러 뒤섞어서 삽입 -> 결과는 시간순이어야 함
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("3000:30.0:1", 3000.0));
        tuples.add(new DefaultTypedTuple<>("1000:10.0:2", 1000.0));
        tuples.add(new DefaultTypedTuple<>("2000:20.0:3", 2000.0));

        when(zSetOperations.rangeByScoreWithScores(EXPECTED_KEY, from.toEpochMilli(), to.toEpochMilli()))
                .thenReturn(tuples);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> result =
                repository.getRange(ROOM_ID, MeasurementType.TEMPERATURE, from, to);

        assertThat(result).extracting(SensorTimeSeriesRepository.TimeSeriesPoint::value)
                .containsExactly(10.0, 20.0, 30.0);
        assertThat(result).extracting(p -> p.timestamp().toEpochMilli())
                .containsExactly(1000L, 2000L, 3000L);
    }

    @Test
    @DisplayName("getRange는 조회 결과가 null이면 빈 리스트를 반환한다")
    void getRange_returnsEmptyListWhenNull() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble())).thenReturn(null);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> result =
                repository.getRange(ROOM_ID, MeasurementType.TEMPERATURE, Instant.ofEpochMilli(0), Instant.ofEpochMilli(1000));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRange는 조회 결과가 빈 Set이면 빈 리스트를 반환한다")
    void getRange_returnsEmptyListWhenEmptySet() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble())).thenReturn(Set.of());

        List<SensorTimeSeriesRepository.TimeSeriesPoint> result =
                repository.getRange(ROOM_ID, MeasurementType.TEMPERATURE, Instant.ofEpochMilli(0), Instant.ofEpochMilli(1000));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRange는 형식이 잘못된 member(콜론 없음)는 무시하고 나머지는 정상 반환한다")
    void getRange_skipsMalformedMember() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Instant from = Instant.ofEpochMilli(0);
        Instant to = Instant.ofEpochMilli(2000);

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("invalid-member", 500.0)); // 콜론 없음 -> 무시
        tuples.add(new DefaultTypedTuple<>("1000:15.0:1", 1000.0));

        when(zSetOperations.rangeByScoreWithScores(EXPECTED_KEY, from.toEpochMilli(), to.toEpochMilli()))
                .thenReturn(tuples);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> result =
                repository.getRange(ROOM_ID, MeasurementType.TEMPERATURE, from, to);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().value()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("getRange는 value 부분이 숫자로 파싱되지 않는 member는 무시한다")
    void getRange_skipsMemberWithUnparsableValue() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Instant from = Instant.ofEpochMilli(0);
        Instant to = Instant.ofEpochMilli(2000);

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("1000:NOT_A_NUMBER:1", 1000.0));
        tuples.add(new DefaultTypedTuple<>("2000:20.0:2", 2000.0));

        when(zSetOperations.rangeByScoreWithScores(EXPECTED_KEY, from.toEpochMilli(), to.toEpochMilli()))
                .thenReturn(tuples);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> result =
                repository.getRange(ROOM_ID, MeasurementType.TEMPERATURE, from, to);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().value()).isEqualTo(20.0);
    }
}