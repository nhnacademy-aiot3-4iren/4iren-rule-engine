package com.nhnacademy.ruleengine.engine.publisher;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AlertEventPublisher alertEventPublisher;

    private final String exchange = "test.exchange";
    private final String routingKey = "test.routing.key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(alertEventPublisher, "alertExchange", exchange);
        ReflectionTestUtils.setField(alertEventPublisher, "alertRoutingKey", routingKey);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private AlertEvent createDummyAlertEvent(Long roomId) {
        return new AlertEvent(
                roomId,
                AlertType.COMFORT_LIMIT_EXCEEDED,
                "온도 초과 경고",
                "dev-123",
                "Device A",
                "Point 1",
                List.of(),
                Instant.now(),
                UUID.randomUUID().toString()
        );
    }

    @Test
    @DisplayName("쿨다운이 없는 경우 알림 발행")
    void publish_success() {
        Long roomId = 101L;
        AlertEvent event = createDummyAlertEvent(roomId);
        String expectedKey = "alert:cooldown:room:" + roomId;

        when(valueOperations.setIfAbsent(expectedKey, "SENT", Duration.ofHours(1)))
                .thenReturn(true);

        alertEventPublisher.publish(event);

        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKey, event);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("발송 이력이 있는 경우 알림 스킵")
    void publish_skipWhenCooldownActive() {
        Long roomId = 101L;
        AlertEvent event = createDummyAlertEvent(roomId);
        String expectedKey = "alert:cooldown:room:" + roomId;

        when(valueOperations.setIfAbsent(expectedKey, "SENT", Duration.ofHours(1)))
                .thenReturn(false);

        alertEventPublisher.publish(event);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("RabbitMQ 메시지 발행 중 예외가 발생하면 쿨다운 키 즉시 삭제")
    void publish_rollbackOnRabbitMqError() {
        Long roomId = 101L;
        AlertEvent event = createDummyAlertEvent(roomId);
        String expectedKey = "alert:cooldown:room:" + roomId;

        when(valueOperations.setIfAbsent(expectedKey, "SENT", Duration.ofHours(1)))
                .thenReturn(true);

        doThrow(new RuntimeException("RabbitMQ Connection Error"))
                .when(rabbitTemplate).convertAndSend(exchange, routingKey, event);

        alertEventPublisher.publish(event);

        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKey, event);
        verify(redisTemplate, times(1)).delete(expectedKey);
    }
}