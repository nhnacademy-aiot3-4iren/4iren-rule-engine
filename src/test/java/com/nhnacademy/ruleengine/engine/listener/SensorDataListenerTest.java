package com.nhnacademy.ruleengine.engine.listener;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidPayloadException;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.engine.converter.SensorPayloadConverter;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorDataListenerTest {

    @Mock
    private SensorPayloadConverter converter;

    @InjectMocks
    private SensorDataListener listener;

    @Test
    @DisplayName("정상 메시지가 수신되면 Converter를 호출")
    void receiveSensorData_Success() {
        String rawMessage = "{\"valid\": \"json\"}";

        EnvironmentContext.MetricInfo metrics = new EnvironmentContext.MetricInfo("temperature", 24.5, "24e124725d081175", Instant.now());
        EnvironmentContext environmentContext = new EnvironmentContext(1L, List.of(metrics), Instant.now());

        given(converter.convert(rawMessage)).willReturn(environmentContext);

        listener.receiveSensorData(rawMessage);

        verify(converter).convert(rawMessage);
    }

    @Test
    @DisplayName("Converter에서 InvalidPayloadException 발생 시 예외를 위로 던져 DLQ 라우팅 유도")
    void receiveSensorData_ThrowsException_WhenPayloadIsInvalid() {
        String rawMessage = "{\"invalid\": \"json\"}";
        given(converter.convert(anyString())).willThrow(new InvalidPayloadException());

        assertThatThrownBy(() -> listener.receiveSensorData(rawMessage))
                .isInstanceOf(InvalidPayloadException.class);

        verify(converter).convert(rawMessage);
    }
}