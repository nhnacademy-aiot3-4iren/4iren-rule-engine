package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
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

        return errors;
    }
    
}
