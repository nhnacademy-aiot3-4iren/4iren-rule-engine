package com.nhnacademy.ruleengine.engine.publisher;

import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String alertExchange;
    @Value("${ruleengine.routing-key.alert}")
    private String alertRoutingKey;

    private static final String DEDUP_KEY_FORMAT = "alert:dedup:node:%d:%s";

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    public void publish(AlertEvent event, Long alertNodeId, Integer dedupWindowSec) {
        Long roomId = event.roomId();
        String alertLockKey = resolveAlertLockKey(event, alertNodeId);
        Duration alertLockTtl = Duration.ofSeconds(dedupWindowSec);

        if(!tryAcquireAlertLock(alertLockKey, alertLockTtl)) {
            log.debug("[ALERT SKIP]");
            return;
        }

        try {
            rabbitTemplate.convertAndSend(alertExchange, alertRoutingKey, event);
            log.info("[ALERT PUBLISH] roomId({}), eventId({}), title({})", roomId, event.eventId(), event.alertTitle());
        } catch (Exception e) {
            log.error("[ALERT ERROR] roomId({}), error({})", roomId, e.getMessage(), e);
            redisTemplate.delete(alertLockKey);
        }

    }

    private boolean tryAcquireAlertLock(String key, Duration ttl) {
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(key, "SENT", ttl);//SETNX: key가 redis에 없을 때만 값을 저장(true 반환).
        return Boolean.TRUE.equals(isFirst);//최초 알람일 경우 true 반환
    }

    private String resolveAlertLockKey(AlertEvent event, Long alertNodeId) {
        return DEDUP_KEY_FORMAT.formatted(alertNodeId, createDedupHash(event));
    }

    private String createDedupHash(AlertEvent event) {
        String source = normalizedNodeResults(event);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");// SHA-256 알고리즘을 사용한 암호화 객체 생성
            return HexFormat.of().formatHex(digest.digest(source.getBytes(StandardCharsets.UTF_8)));//문자열 -> 바이트 배열 변환 -> 해싱 처리 -> 16진수 문자열 변환
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private String normalizedNodeResults(AlertEvent event) {
        if (event.nodeResults() == null || event.nodeResults().isEmpty()) {
            return "";
        }

        return event.nodeResults().stream()
                .filter(Objects::nonNull)
                .map(this::normalizeNodeResult)
                .sorted()
                .collect(Collectors.joining(";"));
    }

    private String normalizeNodeResult(AlertEvent.NodeResult result) {
        return String.join(",",
                valueOf(result.nodeType()),
                valueOf(result.metricType()),
                valueOf(result.operator()),
                valueOf(result.threshold())
        );
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }
}
