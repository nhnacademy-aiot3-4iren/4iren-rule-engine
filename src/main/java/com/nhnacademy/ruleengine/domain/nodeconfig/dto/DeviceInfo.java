package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

public record DeviceInfo(
        String devEui,
        String deviceName
) {
    public static DeviceInfo of(  String devEui, String deviceName){
        return new DeviceInfo(devEui, deviceName);
    }
}
