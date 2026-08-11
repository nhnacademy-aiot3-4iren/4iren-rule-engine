package com.nhnacademy.ruleengine.engine.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidPayloadException;
import com.nhnacademy.ruleengine.engine.model.SensorPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorPayloadConverterTest {

    private SensorPayloadConverter converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        converter = new SensorPayloadConverter(objectMapper);
    }

    @Test
    @DisplayName("정상 JSON 데이터 -> SensorPayload")
    void convert_Success() {
        String validJson = """
                {
                  "device": {
                    "applicationId": "applicationId1",
                    "applicationName": "applicationName1",
                    "deviceProfileId": "deviceProfileId1",
                    "deviceName": "deviceName1",
                    "devEui": "1111111111111111",
                    "roomId": 1,
                    "point": "1번"
                  },
                  "sensorDataList": [
                    {
                      "category": "ENVIRONMENT",
                      "measurement": "temperature",
                      "value": 24.5
                    }
                  ],
                  "measuredAt": "2026-08-10T08:00:00Z"
                }
                """;
        SensorPayload payload = converter.convert(validJson);

        assertThat(payload).isNotNull();
        assertThat(payload.device().roomId()).isEqualTo(1L);
        assertThat(payload.device().devEui()).isEqualTo("1111111111111111");
        assertThat(payload.sensorDataList()).hasSize(1);
        assertThat(payload.sensorDataList().getFirst().value()).isEqualTo(24.5);
        assertThat(payload.measuredAt()).isNotNull();
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
    @DisplayName("device 또는 roomId가 누락된 경우 InvalidPayloadException 던짐")
    void convert_MissingRoomId_ThrowsException() {
        String jsonWithoutRoomId = """
                {
                  "device": {
                    "devEui": "2222222222222222"
                  },
                  "sensorDataList": [{"category": "ENVIRONMENT", "measurement": "temperature", "value": 24.5}],
                  "measuredAt": "2026-08-10T08:00:00Z"
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
    @DisplayName("sensorDataList가 비어있거나 없는 경우 InvalidPayloadException 던짐")
    void convert_EmptySensorDataList_ThrowsException() {
        String jsonWithEmptySensorData = """
                {
                  "device": { "roomId": 3, "devEui": "3333333333333333" },
                  "sensorDataList": [],
                  "measuredAt": "2026-08-10T08:00:00Z"
                }
                """;
        assertThatThrownBy(() -> converter.convert(jsonWithEmptySensorData))
                .isInstanceOf(InvalidPayloadException.class);
    }

    @Test
    @DisplayName("measuredAt 정보가 누락된 경우 InvalidPayloadException 던짐")
    void convert_MissingMeasuredAt_ThrowsException() {
        String jsonWithoutMeasuredAt = """
                {
                  "device": { "roomId": 4, "devEui": "4444444444444444" },
                  "sensorDataList": [{"category": "ENVIRONMENT", "measurement": "temperature", "value": 24.5}]
                }
                """;
        assertThatThrownBy(() -> converter.convert(jsonWithoutMeasuredAt))
                .isInstanceOf(InvalidPayloadException.class);
    }
}