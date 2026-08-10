package com.nhnacademy.ruleengine.domain.nodeconfig.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

// MeasurementType.java
@Getter
public enum MeasurementType {
    CO2("이산화탄소"),
    HUMIDITY("상대습도"),
    ILLUMINATION("주변 조도"),
    INFRARED("적외선"),
    PRESSURE("대기압"),
    TEMPERATURE("온도"),
    TVOC("총휘발성유기화합물 농도")
    ;

    private String sensorDesc;

    MeasurementType(String sensorDesc){
        this.sensorDesc = sensorDesc;
    }

    @JsonCreator
    public static MeasurementType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String upperValue = value.toUpperCase();
        for (MeasurementType type : MeasurementType.values()) {
            if (type.name().equals(upperValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 MeasurementType 입니다: " + value);
    }
}
