package com.nhnacademy.ruleengine.engine.publisher;

import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String alertExchange;
    @Value("${ruleengine.routing-key.alert}")
    private String alertRoutingKey;

    private static final String COOLDOWN_KEY_PREFIX = "alert:cooldown:room:%d";
    private static final Duration COOLDOWN_DURATION = Duration.ofHours(1);

    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    public void publish(AlertEvent event) {
        Long roomId = event.roomId();

        if(!tryAcquireAlertLock(roomId)) {
            log.debug("[ALERT SKIP]");
            return;
        }

        try {
            rabbitTemplate.convertAndSend(alertExchange, alertRoutingKey, event);
            log.info("[ALERT PUBLISH] roomId({}), eventId({}), title({})", roomId, event.eventId(), event.alertTitle());
        } catch (Exception e) {
            log.error("[ALERT ERROR] roomId({}), error({})", roomId, e.getMessage(), e);
            redisTemplate.delete(COOLDOWN_KEY_PREFIX.formatted(roomId));
        }

    }

    private boolean tryAcquireAlertLock(Long roomId) {
        String key = COOLDOWN_KEY_PREFIX.formatted(roomId);
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(key, "SENT", COOLDOWN_DURATION);
        return Boolean.TRUE.equals(isFirst);
    }
}
