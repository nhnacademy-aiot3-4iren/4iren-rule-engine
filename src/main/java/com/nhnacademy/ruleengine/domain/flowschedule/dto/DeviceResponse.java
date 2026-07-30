package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import java.util.Map;

public record DeviceResponse(
        Long roomId,
        String devEui,
        String deviceName,
        Map<String, String> measurement//센서 타입, 단위
) {
}
