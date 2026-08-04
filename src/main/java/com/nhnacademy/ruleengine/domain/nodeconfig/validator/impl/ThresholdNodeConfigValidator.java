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

        boolean validSensorType = sensorStaticMetaList.stream()
                .anyMatch(meta -> meta.measurementType() == thresholdNodeConfig.measurementType());
        if (!validSensorType) {
            errors.add("해당 강의실에서 지원하지 않는 sensorType: " + thresholdNodeConfig.measurementType());
            return errors; // 이후 검증 의미 없음
        }

        // 2. targetDeviceEui가 해당 sensorType의 deviceOptions에 존재하는지
        SensorStaticMeta targetMeta = sensorStaticMetaList.stream()
                .filter(meta -> meta.measurementType() == thresholdNodeConfig.measurementType())
                .findFirst().get();

        boolean validDevice = targetMeta.deviceOptions().stream()
                .anyMatch(device -> device.devEui().equals(thresholdNodeConfig.targetDeviceEui()));
        if (!validDevice) {
            errors.add("해당 강의실에 존재하지 않는 deviceEui: " + thresholdNodeConfig.targetDeviceEui());

        }

        validateThresholdRange(thresholdNodeConfig.measurementType(), thresholdNodeConfig.threshold(), errors);

        return errors;
    }

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
            // 필요한 sensorType 추가
        }
        return errors;
    }

    
}
