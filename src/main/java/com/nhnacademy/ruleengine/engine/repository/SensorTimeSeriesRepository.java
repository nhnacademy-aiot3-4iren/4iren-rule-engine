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
    private static final String KEY_FORMAT = "room:%d:metric:%s:ts";

    public static final Duration MAX_RETENTION = Duration.ofHours(24);

    private String getKey(Long roomId, MeasurementType type) {
        return KEY_FORMAT.formatted(roomId, type.name());
    }

    /**
     * 센서 측정값 저장 및 슬라이딩 윈도우 만료 데이터 제거
     */
    public void save(Long roomId, MeasurementType type, double value, Instant timestamp) {
        String key = getKey(roomId, type);
        long score = timestamp.toEpochMilli();
        String member = score + ":" + value + ":" + ThreadLocalRandom.current().nextInt();

        redisTemplate.opsForZSet().add(key, member, score);

        long expireThreshold = timestamp.minus(MAX_RETENTION).toEpochMilli();
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, expireThreshold);

        redisTemplate.expire(key, MAX_RETENTION);
    }

    /**
     * 특정 시간 범위(window) 동안의 시계열 데이터를 시간순 정렬된 상태로 조회
     */
    public List<TimeSeriesPoint> getRange(Long roomId, MeasurementType type, Instant from, Instant to) {
        String key = getKey(roomId, type);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().rangeByScoreWithScores(key, from.toEpochMilli(), to.toEpochMilli());

        if(tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        return tuples.stream()
                .map(this::toPoint)
                .filter(Objects::nonNull)
                .sorted((a, b) -> a.timestamp().compareTo(b.timestamp()))
                .toList();
    }

    private TimeSeriesPoint toPoint(ZSetOperations.TypedTuple<String> tuple) {
        String member = tuple.getValue();
        Double score = tuple.getScore();
        if(member == null || score == null) {
            return null;
        }

        String[] parts = member.split(":", 3);
        if(parts.length < 2) {
            log.warn("잘못된 형식의 시계열 member 무시: {}", member);
            return null;
        }

        try {
            double value = Double.parseDouble(parts[1]);
            return new TimeSeriesPoint(Instant.ofEpochMilli(score.longValue()), value);
        } catch (NumberFormatException e) {
            log.warn("시계열 member 파싱 실패: {}", member, e);
            return null;
        }
    }

    public record TimeSeriesPoint(
            Instant timestamp,
            double value
    ) {}
}
