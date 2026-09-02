package com.nhnacademy.ruleengine.domain.nodeconfig.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidMeasurementTypeException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum MeasurementType {
    CO2("이산화탄소"),
    HUMIDITY("상대습도"),
    ILLUMINATION("주변 조도"),
    TEMPERATURE("온도"),
    PRESSURE("대기압"),
    TVOC("총유기화합물"),
    INFRARED("적외선");

    private final String sensorDesc;

    private static final Map<String, MeasurementType> TYPE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(type -> type.name().toUpperCase(), Function.identity()));

    MeasurementType(String sensorDesc){
        this.sensorDesc = sensorDesc;
    }

    @JsonCreator
    public static MeasurementType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        MeasurementType type = TYPE_MAP.get(value.toUpperCase());
        if (type != null) {
            return type;
        }
        throw new InvalidMeasurementTypeException();
    }

    public static List<String> toNames(List<MeasurementType> measurementTypes) {
        if (measurementTypes == null || measurementTypes.isEmpty()) {
            return List.of();
        }

        return measurementTypes.stream()
                .map(MeasurementType::name)
                .toList();
    }
}