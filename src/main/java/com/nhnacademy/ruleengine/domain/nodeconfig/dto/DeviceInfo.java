package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장비 정보")
public record DeviceInfo(
        @Schema(description = "장비 고유 식별자", example = "24E124128C123456")
        String devEui,
        @Schema(description = "장비 이름", example = "강의실 온습도 센서")
        String deviceName
) {
    public static DeviceInfo of(  String devEui, String deviceName){
        return new DeviceInfo(devEui, deviceName);
    }
}
