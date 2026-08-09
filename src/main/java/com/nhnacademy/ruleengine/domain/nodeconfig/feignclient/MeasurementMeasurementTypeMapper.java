package com.nhnacademy.ruleengine.domain.nodeconfig.feignclient;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class MeasurementMeasurementTypeMapper {
    //TODO MeasurementType, 단위 processing에게 의존하도록? 혹은 매핑 방식을 동적으로? 암튼 하드코딩 안됨
    private final Map<String, MeasurementType> measurementTypeMap = Map.of(
            "co2", MeasurementType.CO2,
            "temperature", MeasurementType.TEMPERATURE,
            "humidity", MeasurementType.HUMIDITY,
            "pressure", MeasurementType.PRESSURE,
            "tvoc", MeasurementType.TVOC,
            "illumination", MeasurementType.ILLUMINATION,
            "infrared", MeasurementType.INFRARED
    );

    public Optional<MeasurementType> toMeasurementType(String measurementKey){
        if(measurementKey == null || measurementKey.isBlank()){
            return Optional.empty();
        }

        return Optional.ofNullable(measurementTypeMap.get(measurementKey.toLowerCase()));
    }
}
