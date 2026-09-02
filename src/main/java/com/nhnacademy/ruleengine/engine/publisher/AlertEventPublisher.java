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
            //false: 락 획득 실패(이미 최근에 같은 알림이 나갔음) → 상위 호출자 입장에서는 예외 없이 그냥 "발행 안 함"으로 끝남
            log.info("[ALERT SKIP]");
            return;
        }

        try {
            //락 획득 성공시 큐에 메시지 발행, event 객체 자동 직렬화 -> 메시지 바디로 들어감
            rabbitTemplate.convertAndSend(alertExchange, alertRoutingKey, event);
            log.info("[ALERT PUBLISH] roomId({}), eventId({}), title({})", roomId, event.eventId(), event.alertTitle());
        } catch (Exception e) {
            //실패시 락 지움
            log.error("[ALERT ERROR] roomId({}), error({})", roomId, e.getMessage(), e);
            redisTemplate.delete(alertLockKey);
        }

    }

    //락시도,
    private boolean tryAcquireAlertLock(String key, Duration ttl) {
        //SENTX + TTL 방식의 분살 락
        //기존 키가 없었으면 "SENT"로 저장하고 true 반환 → 알림 발행 진행
        //키가 이미 있으면 false 반환 → 스킵
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(key, "SENT", ttl);//SETNX: key가 redis에 없을 때만 값을 저장(true 반환).
        return Boolean.TRUE.equals(isFirst);//isFirst == null 일경우 false 처리됨
    }

    //락 키 생성, (알림 노드 ID + event history 해시값)
    private String resolveAlertLockKey(AlertEvent event, Long alertNodeId) {
        return DEDUP_KEY_FORMAT.formatted(alertNodeId, createDedupHash(event));
    }

    //이벤트 내용 해싱
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
                .sorted()//정렬이 없으면 같은 내용이 순서만 달라서 해시값이 달라질 수 있음
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
