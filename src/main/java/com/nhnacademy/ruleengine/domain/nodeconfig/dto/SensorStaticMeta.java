package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import lombok.Builder;

import java.util.List;

@Builder
public record SensorStaticMeta(
        SensorType sensorType,
        String unit,
        List<DeviceInfo> deviceOptions
) {
    public static  SensorStaticMeta of(SensorType sensorType, String unit, List<DeviceInfo> deviceOptions){
        return SensorStaticMeta.builder().sensorType(sensorType).unit(unit).deviceOptions(deviceOptions).build();
    }
}
