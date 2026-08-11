package com.nhnacademy.ruleengine.common.external;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class MeasurementMeasurementTypeMapper {
    //TODO MeasurementType, 단위 등 측정 데이터 정보에 대한 건 processing에게 의존하도록 바꿔야함. MeasurementType enum 따로 관리 안하도록
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
