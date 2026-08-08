package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ThresholdNodeConfigValidator implements NodeConfigValidator {
    @Override
    public NodeType supportsNodeType() {
        return NodeType.THRESHOLD;
    }

    @Override
    public List<String> validate(NodeConfig nodeConfig, List<SensorStaticMeta> sensorStaticMetaList) {
        ThresholdNodeConfig thresholdNodeConfig = (ThresholdNodeConfig) nodeConfig;
        List<String > errors = new ArrayList<>();

        boolean validMeasurementType = sensorStaticMetaList.stream()
                .anyMatch(meta -> meta.measurementType() == thresholdNodeConfig.measurementType());
        if (!validMeasurementType) {
            errors.add("해당 강의실에서 지원하지 않는 measurementType: " + thresholdNodeConfig.measurementType());
            return errors; // 이후 검증 의미 없음
        }

        // 2. targetDeviceEui가 해당 measurementType의 deviceOptions에 존재하는지
        SensorStaticMeta targetMeta = sensorStaticMetaList.stream()
                .filter(meta -> meta.measurementType() == thresholdNodeConfig.measurementType())
                .findFirst().get();



        validateThresholdRange(thresholdNodeConfig.measurementType(), thresholdNodeConfig.threshold(), errors);

        return errors;
    }

    //TODO util클래스로 빼기
    private List<String>  validateThresholdRange(MeasurementType measurementType, double threshold, List<String> errors) {
        switch (measurementType) {
            case TEMPERATURE -> {
                if (threshold < -50 || threshold > 100)
                    errors.add("온도 임계값 범위 초과 (-50 ~ 100): " + threshold);
            }
            case HUMIDITY -> {
                if (threshold < 0 || threshold > 100)
                    errors.add("습도 임계값 범위 초과 (0 ~ 100): " + threshold);
            }
            case CO2 -> {
                if (threshold < 0 || threshold > 10000)
                    errors.add("CO2 임계값 범위 초과 (0 ~ 10000): " + threshold);
            }
            // 필요한 measurementType 추가
        }
        return errors;
    }

    
}
