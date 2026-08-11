package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record ExternalRoomDeviceInfo(
        Long roomId,
        String devEui,
        String deviceName,
        Map<String, String> measurement//<센서 타입, 단위>
) {
}
