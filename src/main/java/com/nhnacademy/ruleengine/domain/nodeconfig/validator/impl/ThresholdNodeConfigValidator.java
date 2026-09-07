package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse.NodeConfigError;
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
    public List<NodeConfigError> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        ThresholdNodeConfig thresholdNodeConfig = (ThresholdNodeConfig) nodeConfig;
        List<NodeConfigError> errors = new ArrayList<>();

        if (thresholdNodeConfig.measurementType() == null) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "측정 항목을 선택해야 합니다."));
            return errors;
        }
        if (thresholdNodeConfig.operator() == null) {
            errors.add(NodeConfigError.of("nodeConfig.operator", "비교 조건을 선택해야 합니다."));
        }
        if (thresholdNodeConfig.unit() == null || thresholdNodeConfig.unit().isBlank()) {
            errors.add(NodeConfigError.of("nodeConfig.unit", "단위를 입력해야 합니다."));
        }
        if(thresholdNodeConfig.threshold() == null){
            errors.add(NodeConfigError.of("nodeConfig.threshold", "기준값을 입력해야 합니다."));
        }

        boolean validMeasurementType = sensorMetaInfoList.stream()
                .anyMatch(meta -> meta.measurementType() == thresholdNodeConfig.measurementType());
        if (!validMeasurementType) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "이 강의실에서 사용할 수 없는 측정 항목입니다: " + thresholdNodeConfig.measurementType().getSensorDesc()));
            return errors; // 이후 검증 의미 없음
        }

        return errors;
    }
    
}
