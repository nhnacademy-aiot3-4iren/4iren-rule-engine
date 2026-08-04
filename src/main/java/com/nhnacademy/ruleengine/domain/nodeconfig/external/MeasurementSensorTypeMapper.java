package com.nhnacademy.ruleengine.domain.nodeconfig.external;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class MeasurementSensorTypeMapper {
    private final Map<String, MeasurementType> sensorTypeMap = Map.of(
            "co2", MeasurementType.CO2,
            "temperature", MeasurementType.TEMPERATURE,
            "humidity", MeasurementType.HUMIDITY,
            "pressure", MeasurementType.PRESSURE,
            "tvoc", MeasurementType.TVOC,
            "illumination", MeasurementType.ILLUMINATION,
            "infrared", MeasurementType.INFRARED
    );

    public Optional<MeasurementType> toSensorType(String measurementKey){
        if(measurementKey == null || measurementKey.isBlank()){
            return Optional.empty();
        }

        return Optional.ofNullable(sensorTypeMap.get(measurementKey.toLowerCase()));
    }
}
