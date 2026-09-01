package com.nhnacademy.ruleengine.engine.publisher;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    private AlertEvent createAlertEvent(Long roomId, List<AlertEvent.NodeResult> nodeResults) {
        return new AlertEvent(
                roomId,
                AlertType.COMFORT_LIMIT_EXCEEDED,
                "온도 초과 경고",
                "dev-123",
                "Device A",
                "Point 1",
                nodeResults,
                Instant.now(),
                UUID.randomUUID().toString()
        );
    }

    @Test
    @DisplayName("history 기반 해시 키와 dedupWindowSec TTL로 알림 발행")
    void publish_success() {
        AlertEvent event = createAlertEvent(101L, List.of(
                new AlertEvent.NodeResult("THRESHOLD", "TEMPERATURE", "GREATER_THAN", "C", 30.0, 31.5)
        ));
        int dedupWindowSec = 30;

        when(valueOperations.setIfAbsent(anyString(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec))))
                .thenReturn(true);

        alertEventPublisher.publish(event, 10L, dedupWindowSec);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).setIfAbsent(keyCaptor.capture(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec)));
        assertThat(keyCaptor.getValue()).startsWith("alert:dedup:node:10:");
        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKey, event);
    }

    @Test
    @DisplayName("중복 감지 키가 이미 있으면 알림 스킵")
    void publish_skipWhenDedupKeyExists() {
        AlertEvent event = createDummyAlertEvent(101L);
        int dedupWindowSec = 30;

        when(valueOperations.setIfAbsent(anyString(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec))))
                .thenReturn(false);

        alertEventPublisher.publish(event, 10L, dedupWindowSec);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("RabbitMQ 메시지 발행 중 예외가 발생하면 dedup 키 즉시 삭제")
    void publish_rollbackOnRabbitMqError() {
        AlertEvent event = createDummyAlertEvent(101L);
        int dedupWindowSec = 30;

        when(valueOperations.setIfAbsent(anyString(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec))))
                .thenReturn(true);

        doThrow(new RuntimeException("RabbitMQ Connection Error"))
                .when(rabbitTemplate).convertAndSend(exchange, routingKey, event);

        alertEventPublisher.publish(event, 10L, dedupWindowSec);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).setIfAbsent(keyCaptor.capture(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec)));
        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKey, event);
        verify(redisTemplate, times(1)).delete(keyCaptor.getValue());
    }


    @Test
    @DisplayName("OR 노드 병합 history 순서가 달라도 같은 중복 감지 해시 키를 사용")
    void publish_createSameDedupKeyForSameOrMergedHistoryRegardlessOfOrder() {
        AlertEvent.NodeResult thresholdResult = new AlertEvent.NodeResult("THRESHOLD", "TEMPERATURE", "GREATER_THAN", "C", 30.0, 31.5);
        AlertEvent.NodeResult gradientResult = new AlertEvent.NodeResult("GRADIENT", "TEMPERATURE", "GREATER_THAN", "C", 3.0, 3.2);
        AlertEvent.NodeResult orResult = new AlertEvent.NodeResult("OR", null, null, null, null, null);
        AlertEvent event = createAlertEvent(101L, List.of(thresholdResult, gradientResult, orResult));
        AlertEvent sameEventWithReorderedHistory = createAlertEvent(101L, List.of(orResult, gradientResult, thresholdResult));
        int dedupWindowSec = 60;

        when(valueOperations.setIfAbsent(anyString(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec))))
                .thenReturn(true, false);

        alertEventPublisher.publish(event, 10L, dedupWindowSec);
        alertEventPublisher.publish(sameEventWithReorderedHistory, 10L, dedupWindowSec);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations, times(2)).setIfAbsent(keyCaptor.capture(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec)));
        assertThat(keyCaptor.getAllValues().get(0)).isEqualTo(keyCaptor.getAllValues().get(1));
        verify(rabbitTemplate, times(1)).convertAndSend(exchange, routingKey, event);
    }

    @Test
    @DisplayName("같은 history라도 알림 노드가 다르면 다른 중복 감지 키를 사용")
    void publish_createDifferentDedupKeyForDifferentAlertNode() {
        AlertEvent event = createAlertEvent(101L, List.of(
                new AlertEvent.NodeResult("THRESHOLD", "TEMPERATURE", "GREATER_THAN", "C", 30.0, 31.5)
        ));
        int dedupWindowSec = 60;

        when(valueOperations.setIfAbsent(anyString(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec))))
                .thenReturn(true, true);

        alertEventPublisher.publish(event, 10L, dedupWindowSec);
        alertEventPublisher.publish(event, 11L, dedupWindowSec);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations, times(2)).setIfAbsent(keyCaptor.capture(), eq("SENT"), eq(Duration.ofSeconds(dedupWindowSec)));
        assertThat(keyCaptor.getAllValues().get(0)).isNotEqualTo(keyCaptor.getAllValues().get(1));
        verify(rabbitTemplate, times(2)).convertAndSend(exchange, routingKey, event);
    }
}
