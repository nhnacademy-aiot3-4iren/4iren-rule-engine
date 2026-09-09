package com.nhnacademy.ruleengine.engine.repository;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorTimeSeriesRepository {

    private final StringRedisTemplate redisTemplate;
    // Average/Duration처럼 방 전체 기준으로 조회하는 시계열 키 형식이다.
    private static final String KEY_FORMAT = "room:%d:metric:%s:ts";
    // Gradient처럼 같은 장비(devEui)의 변화만 봐야 하는 시계열 키 형식이다.
    private static final String DEVICE_KEY_FORMAT = "room:%d:metric:%s:device:%s:ts";

    public static final Duration MAX_RETENTION = Duration.ofHours(1);

    private String getKey(Long roomId, MeasurementType type) {
        return KEY_FORMAT.formatted(roomId, type.name());
    }
    private String getDeviceKey(Long roomId, MeasurementType type, String devEui) {
        return DEVICE_KEY_FORMAT.formatted(roomId, type.name(), devEui);
    }

    // 방 단위 센서 측정값을 기본 최대 보관 시간으로 저장
    public void save(Long roomId, MeasurementType type, double value, Instant timestamp) {
        savePoint(getKey(roomId, type), value, timestamp);
    }

    //장비 단위 센서 측정값을 기본 최대 보관 시간으로 저장
    public void save(Long roomId, MeasurementType type, String devEui, double value, Instant timestamp) {
        savePoint(getDeviceKey(roomId, type, devEui), value, timestamp);
    }

    /**
     * Redis ZSet 구조를 이용해 시계열 센서 데이터를 저장하고 오래된 데이터를 제거한다.
     */
    private void savePoint(String key, double value, Instant timestamp) {
        // ZSet score는 숫자 정렬 기준이므로, 시간순 정렬이 가능하도록 timestamp를 millisecond로 변환해 사용한다.
        long score = timestamp.toEpochMilli();
        // 같은 millisecond에 같은 값이 여러 번 들어와도 ZSet member가 덮어써지지 않도록 난수를 붙인다.
        String member = score + ":" + value + ":" + ThreadLocalRandom.current().nextInt();

        // Redis ZSet은 score 기준으로 member를 정렬하므로 시간 범위 조회에 적합하다.
        redisTemplate.opsForZSet().add(key, member, score);

        // 현재 데이터 시각에서 MAX_RETENTION보다 오래된 데이터의 score 경계값을 계산한다.
        long expireThreshold = timestamp.minus(MAX_RETENTION).toEpochMilli();
        // 경계값 이하의 과거 데이터를 삭제해 Redis에 필요한 윈도우만 남긴다.
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, expireThreshold);

        // 새 데이터가 더 이상 들어오지 않는 key도 자동으로 사라지도록 key TTL을 갱신한다.
        redisTemplate.expire(key, MAX_RETENTION);
    }

    // 방 단위 시계열에서 특정 시간 범위(window)의 데이터를 시간순으로 조회한다.
    public List<TimeSeriesPoint> getRange(Long roomId, MeasurementType type, Instant from, Instant to) {
        String key = getKey(roomId, type);
        return getRange(key, from, to);
    }

    /**
     * 방 단위 시계열에서 특정 시각 이전 또는 같은 시각의 최신 데이터를 1개 조회한다.
     */
    public TimeSeriesPoint getLatestBeforeOrAt(Long roomId, MeasurementType type, Instant timestamp) {
        String key = getKey(roomId, type);
        return getLatestBeforeOrAt(key, timestamp);
    }

    /**
     * 장비 단위 시계열에서 특정 시간 범위(window)의 데이터를 시간순으로 조회한다.
     */
    public List<TimeSeriesPoint> getRange(Long roomId, MeasurementType type, String devEui, Instant from, Instant to) {
        String key = getDeviceKey(roomId, type, devEui);
        return getRange(key, from, to);
    }

    /**
     * Redis key 기준으로 score 범위에 해당하는 시계열 데이터를 조회한다.
     */
    private List<TimeSeriesPoint> getRange(String key, Instant from, Instant to) {
        // score를 timestamp millisecond로 저장했기 때문에 from~to도 millisecond로 바꿔 범위 조회한다.
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().rangeByScoreWithScores(key, from.toEpochMilli(), to.toEpochMilli());

        if(tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        // Redis에서 가져온 문자열 member를 TimeSeriesPoint로 변환하고, 혹시 모를 순서 흔들림을 막기 위해 한 번 더 시간순 정렬한다.
        return tuples.stream()
                .map(this::toPoint)
                .filter(Objects::nonNull)
                .sorted((a, b) -> a.timestamp().compareTo(b.timestamp()))
                .toList();
    }

    /**
     * Redis key 기준으로 timestamp 이하의 데이터 중 가장 최신 값을 조회한다.
     */
    private TimeSeriesPoint getLatestBeforeOrAt(String key, Instant timestamp) {
        // reverseRangeByScoreWithScores는 score 내림차순으로 가져오므로 offset=0,count=1이면 가장 최신 데이터 1개다.
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, timestamp.toEpochMilli(), 0, 1);

        if(tuples == null || tuples.isEmpty()) {
            return null;
        }

        return tuples.stream()
                .map(this::toPoint)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Redis에서 꺼낸 ZSet tuple을 TimeSeriesPoint로 변환한다.
     */
    private TimeSeriesPoint toPoint(ZSetOperations.TypedTuple<String> tuple) {
        String member = tuple.getValue();
        Double score = tuple.getScore();
        if(member == null || score == null) {
            return null;
        }

        // member 형식은 "timestamp:value:random"이므로 value만 꺼내기 위해 콜론 기준으로 최대 3조각으로 나눈다.
        String[] parts = member.split(":", 3);
        if(parts.length < 2) {
            log.warn("잘못된 형식의 시계열 member 무시: {}", member);
            return null;
        }

        try {
            double value = Double.parseDouble(parts[1]);
            // timestamp는 member 문자열이 아니라 ZSet score를 기준으로 복원한다.
            return new TimeSeriesPoint(Instant.ofEpochMilli(score.longValue()), value);
        } catch (NumberFormatException e) {
            log.warn("시계열 member 파싱 실패: {}", member, e);
            return null;
        }
    }

    /**
     * 시계열 한 지점의 측정 시각과 측정값을 담는 값 객체다.
     */
    public record TimeSeriesPoint(
            Instant timestamp,
            double value
    ) {}
}
