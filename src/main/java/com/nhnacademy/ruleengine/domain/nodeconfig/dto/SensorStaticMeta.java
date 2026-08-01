package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;

import java.util.List;

public record SensorStaticMeta(
        SensorType sensorType,
        String unit,
        List<DeviceInfo> deviceOptions
) {

}
