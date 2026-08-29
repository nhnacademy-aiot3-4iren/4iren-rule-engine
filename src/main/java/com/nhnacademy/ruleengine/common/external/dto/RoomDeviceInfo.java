package com.nhnacademy.ruleengine.common.external.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record RoomDeviceInfo(
        Long roomId,
        String devEui,
        String deviceName,
        Map<String, String> measurement//<센서 타입, 단위>
) {
}
