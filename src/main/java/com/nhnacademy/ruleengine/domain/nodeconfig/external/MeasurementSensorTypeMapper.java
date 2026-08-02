package com.nhnacademy.ruleengine.domain.nodeconfig.external;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class MeasurementSensorTypeMapper {
    private final Map<String, SensorType> sensorTypeMap = Map.of(
            "co2", SensorType.CO2,
            "temperature", SensorType.TEMPERATURE,
            "humidity", SensorType.HUMIDITY,
            "pressure", SensorType.PRESSURE,
            "tvoc", SensorType.TVOC,
            "illumination", SensorType.ILLUMINATION,
            "infrared", SensorType.INFRARED
    );

    public Optional<SensorType> toSensorType(String measurementKey){
        if(measurementKey == null || measurementKey.isBlank()){
            return Optional.empty();
        }

        return Optional.ofNullable(sensorTypeMap.get(measurementKey.toLowerCase()));
    }
}
