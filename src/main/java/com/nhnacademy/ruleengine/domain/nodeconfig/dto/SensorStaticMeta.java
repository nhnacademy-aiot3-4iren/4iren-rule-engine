package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

@Builder
public record SensorStaticMeta(
        MeasurementType measurementType,
        String unit
        /*
        MeasurementType measurementTypeDesc
         */
) {
    public static  SensorStaticMeta of(MeasurementType measurementType, String unit){
        return SensorStaticMeta.builder().measurementType(measurementType).unit(unit).build();
    }
}
