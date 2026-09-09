package com.nhnacademy.ruleengine.engine.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidPayloadException;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorPayloadConverter {

    private final ObjectMapper objectMapper;


    //변수나 객체에 값이 할당되어 있다면, 해당 값을 다른 형식으로 변환
    public Optional<EnvironmentContext> convertIfAssigned(String rawMessage) {
        try {
            EnvironmentContext payload = objectMapper.readValue(rawMessage, EnvironmentContext.class);
            if (payload == null) {
                throw new IllegalArgumentException("Payload가 null입니다.");
            }
            if (payload.roomId() == null) {
                //방 배정되지 않은 센서 페이로드 스킵"
                return Optional.empty();
            }

            validate(payload);
            log.info("파싱 성공");
            return Optional.of(payload);
        } catch (Exception e) {
            log.warn("센서 페이로드 파싱 또는 검증 실패", e);
            log.info("파싱 실패한 원본 센서 메시지: {}", rawMessage);
            throw new InvalidPayloadException("유효하지 않은 센서 페이로드입니다.", e);
        }
    }

    private void validate(EnvironmentContext payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload가 null입니다.");
        }
        if (payload.metrics() == null || payload.metrics().isEmpty()) {
            throw new IllegalArgumentException("측정 데이터(metrics)가 비어 있습니다.");
        }
        for (EnvironmentContext.MetricInfo metricInfo : payload.metrics()) {
            if (metricInfo == null || metricInfo.metric() == null || metricInfo.value() == null || metricInfo.devEui() == null|| metricInfo.updatedAt() == null) {
                throw new IllegalArgumentException("유효하지 않은 센서 데이터 항목이 포함되어 있습니다.");
            }
        }
        if (payload.updatedAt( ) == null ) {
            throw new IllegalArgumentException("상태 갱신 시간(updatedAt) 정보가 누락되었습니다.");
        }
    }
}
