package com.nhnacademy.ruleengine.engine.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidPayloadException; // 커스텀 예외
import com.nhnacademy.ruleengine.engine.model.SensorPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorPayloadConverter {

    private final ObjectMapper objectMapper;

    public SensorPayload convert(String rawMessage) {
        try {
            SensorPayload payload = objectMapper.readValue(rawMessage, SensorPayload.class);
            validate(payload);
            return payload;
        } catch (Exception e) {
            log.error("SensorPayload 파싱 및 검증 실패 - Raw Message: {}", rawMessage, e);
            throw new InvalidPayloadException("유효하지 않은 센서 페이로드입니다.", e);
        }
    }

    private void validate(SensorPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload가 null입니다.");
        }
        if (payload.device() == null || payload.device().roomId() == null) {
            throw new IllegalArgumentException("DeviceIdentity 또는 roomId 정보가 누락되었습니다.");
        }
        if (payload.sensorDataList() == null || payload.sensorDataList().isEmpty()) {
            throw new IllegalArgumentException("측정 데이터(sensorDataList)가 비어 있습니다.");
        }
        for (SensorPayload.SensorData data : payload.sensorDataList()) {
            if (data == null || data.measurement() == null || data.value() == null) {
                throw new IllegalArgumentException("유효하지 않은 센서 데이터 항목이 포함되어 있습니다.");
            }
        }
        if (payload.measuredAt() == null) {
            throw new IllegalArgumentException("측정 시각(measuredAt) 정보가 누락되었습니다.");
        }
    }
}