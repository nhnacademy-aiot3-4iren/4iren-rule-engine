package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

import java.util.List;

@Builder
public record SensorStaticMeta(
        MeasurementType measurementType,
        String unit,
        List<DeviceInfo> deviceOptions
        /*
        MeasurementType measurementTypeDesc
         */
) {
    public static  SensorStaticMeta of(MeasurementType measurementType, String unit, List<DeviceInfo> deviceOptions){
        return SensorStaticMeta.builder().measurementType(measurementType).unit(unit).deviceOptions(deviceOptions).build();
    }
}
