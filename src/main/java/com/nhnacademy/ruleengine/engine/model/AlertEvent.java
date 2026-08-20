package com.nhnacademy.ruleengine.engine.model;


import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;

import java.time.Instant;
import java.util.List;

public record AlertEvent(
        Long roomId,
        AlertType alertType,
        String alertTitle,
        String deviceEui, //nullable
        String deviceName, //nullable
        String point, //센서 위치, nullable
        List<NodeResult> nodeResults,
        Instant detectedAt,
        String eventId // 멱등성 키
) {
    public record NodeResult(
            String nodeType, // nullable
            String metricType, //TEMPERATURE/HUMIDITY/CO2 등
            String operator,// nullable
            String unit,
            Double threshold,
            Double value
    ){}
}
