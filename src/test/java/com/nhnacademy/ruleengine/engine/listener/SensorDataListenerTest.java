package com.nhnacademy.ruleengine.engine.listener;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidPayloadException;
import com.nhnacademy.ruleengine.engine.converter.SensorPayloadConverter;
import com.nhnacademy.ruleengine.engine.handler.RuleEngineHandler;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SensorDataListenerTest {

    @Mock
    private SensorPayloadConverter converter;

    @Mock
    private RuleEngineHandler handler;

    @InjectMocks
    private SensorDataListener listener;

    @Test
    @DisplayName("정상 메시지가 수신되면 Converter를 호출")
    void receiveSensorData_Success() {
        String rawMessage = "{\"valid\": \"json\"}";

        EnvironmentContext.MetricInfo metrics = new EnvironmentContext.MetricInfo("temperature", 24.5, "24e124725d081175", Instant.now());
        EnvironmentContext environmentContext = new EnvironmentContext(1L, List.of(metrics), Instant.now());

        given(converter.convertIfAssigned(rawMessage)).willReturn(Optional.of(environmentContext));
        given(handler.process(environmentContext)).willReturn(CompletableFuture.completedFuture(null));

        listener.receiveSensorData(message(rawMessage));

        verify(converter).convertIfAssigned(rawMessage);
        verify(handler).process(environmentContext);
    }

    @Test
    @DisplayName("방 배정 없는 메시지는 처리하지 않고 스킵")
    void receiveSensorData_Skip_WhenRoomIsUnassigned() {
        String rawMessage = "{\"device\":{\"roomId\":null},\"sensorDataList\":[]}";
        given(converter.convertIfAssigned(rawMessage)).willReturn(Optional.empty());

        listener.receiveSensorData(message(rawMessage));

        verify(converter).convertIfAssigned(rawMessage);
        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("비동기 룰 엔진 처리 실패 시 예외를 위로 던져 retry/DLQ 라우팅 유도")
    void receiveSensorData_ThrowsException_WhenAsyncRuleEngineFails() {
        String rawMessage = "{\"valid\": \"json\"}";
        EnvironmentContext environmentContext = new EnvironmentContext(1L, List.of(), Instant.now());
        RuntimeException cause = new RuntimeException("flow failed");

        given(converter.convertIfAssigned(rawMessage)).willReturn(Optional.of(environmentContext));
        given(handler.process(environmentContext)).willReturn(CompletableFuture.failedFuture(cause));

        assertThatThrownBy(() -> listener.receiveSensorData(message(rawMessage)))
                .hasCause(cause);

        verify(converter).convertIfAssigned(rawMessage);
        verify(handler).process(environmentContext);
    }

    @Test
    @DisplayName("Converter에서 InvalidPayloadException 발생 시 예외를 위로 던져 DLQ 라우팅 유도")
    void receiveSensorData_ThrowsException_WhenPayloadIsInvalid() {
        String rawMessage = "{\"invalid\": \"json\"}";
        given(converter.convertIfAssigned(anyString())).willThrow(new InvalidPayloadException());

        assertThatThrownBy(() -> listener.receiveSensorData(message(rawMessage)))
                .isInstanceOf(InvalidPayloadException.class);

        verify(converter).convertIfAssigned(rawMessage);
    }

    private Message message(String rawMessage) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        return new Message(rawMessage.getBytes(StandardCharsets.UTF_8), properties);
    }
}
