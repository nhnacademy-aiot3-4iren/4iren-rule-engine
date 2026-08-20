package com.nhnacademy.ruleengine.engine.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidPayloadException;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentContextConverterTest {

    private SensorPayloadConverter converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        converter = new SensorPayloadConverter(objectMapper);
    }

    @Test
    @DisplayName("정상 JSON 데이터 -> EnvironmentContext")
    void convert_Success() {
        String validJson = """
                {
                  "roomId": 1,
                  "metrics": [
                    {
                      "metric": "temperature",
                      "value": 24.5,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:00Z"
                    },
                    {
                      "metric": "humidity",
                      "value": 58.2,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:01Z"
                    },
                    {
                      "metric": "co2",
                      "value": 642.0,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:02Z"
                    }
                  ],
                  "updatedAt": "2026-08-13T05:00:02Z"
                }
                """;
        EnvironmentContext payload = converter.convert(validJson);

        assertThat(payload).isNotNull();
        assertThat(payload.roomId()).isEqualTo(1L);
        assertThat(payload.metrics().getFirst().devEui()).isEqualTo("A84041B2C3D4E5F6");
        assertThat(payload.metrics()).hasSize(3);
        assertThat(payload.metrics().getFirst().value()).isEqualTo(24.5);
        assertThat(payload.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("JSON 형식이 깨진 경우 InvalidPayloadException 던짐")
    void convert_InvalidJsonFormat_ThrowsException() {
        String invalidJson = "{ invalid json string ";

        assertThatThrownBy(() -> converter.convert(invalidJson))
                .isInstanceOf(InvalidPayloadException.class)
                .hasMessageContaining("유효하지 않은 센서 페이로드입니다.");
    }

    @Test
    @DisplayName(" roomId가 누락된 경우 InvalidPayloadException 던짐")
    void convert_MissingRoomId_ThrowsException() {
        String jsonWithoutRoomId = """
                {
                  "metrics": [
                    {
                      "metric": "temperature",
                      "value": 24.5,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:00Z"
                    },
                    {
                      "metric": "humidity",
                      "value": 58.2,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:01Z"
                    },
                    {
                      "metric": "co2",
                      "value": 642.0,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:02Z"
                    }
                  ],
                  "updatedAt": "2026-08-13T05:00:02Z"
                }
                """;

        assertThatThrownBy(() -> converter.convert(jsonWithoutRoomId))
                .isInstanceOf(InvalidPayloadException.class);
    }

    @Test
    @DisplayName("payload가 null인 경우 InvalidPayloadException 던짐")
    void convert_Null_ThrowsException() {
        assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(InvalidPayloadException.class);
    }

    @Test
    @DisplayName("metrics가 비어있거나 없는 경우 InvalidPayloadException 던짐")
    void convert_EmptyMetrics_ThrowsException() {
        String jsonWithEmptySensorData = """
                 {
                  "roomId": 1,
                  "metrics": [],
                  "updatedAt": "2026-08-13T05:00:02Z"
                }
                """;
        assertThatThrownBy(() -> converter.convert(jsonWithEmptySensorData))
                .isInstanceOf(InvalidPayloadException.class);
    }

    @Test
    @DisplayName("updatedAt 정보가 누락된 경우 InvalidPayloadException 던짐")
    void convert_MissingUpdatedAt_ThrowsException() {
        String jsonWithoutMeasuredAt = """
                {
                  "roomId": 1,
                  "metrics": [
                    {
                      "metric": "temperature",
                      "value": 24.5,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:00Z"
                    },
                    {
                      "metric": "humidity",
                      "value": 58.2,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:01Z"
                    },
                    {
                      "metric": "co2",
                      "value": 642.0,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-08-13T05:00:02Z"
                    }
                  ]
                }
                """;
        assertThatThrownBy(() -> converter.convert(jsonWithoutMeasuredAt))
                .isInstanceOf(InvalidPayloadException.class);
    }
}