package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import java.util.Map;

public record DevNSensorTypeInfo(
        Long roomId,
        String devEui,
        String deviceName,
        Map<String, String> measurement//센서 타입, 단위
) {
}
