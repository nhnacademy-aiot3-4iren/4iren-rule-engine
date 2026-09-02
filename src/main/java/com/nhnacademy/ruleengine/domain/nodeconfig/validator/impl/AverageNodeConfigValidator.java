package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse.NodeConfigError;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AverageNodeConfigValidator implements NodeConfigValidator {
    @Override
    public NodeType supportsNodeType() {
        return NodeType.AVERAGE;
    }

    @Override
    public List<NodeConfigError> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        AverageNodeConfig c = (AverageNodeConfig) nodeConfig;
        List<NodeConfigError> errors = new ArrayList<>();


        if (c.measurementType() == null) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "measurementType은 필수입니다"));
            return errors;
        }
        if (c.operator() == null) {
            errors.add(NodeConfigError.of("nodeConfig.operator", "operator는 필수입니다"));
        }
        if (c.unit() == null || c.unit().isBlank()) {
            errors.add(NodeConfigError.of("nodeConfig.unit", "unit은 필수입니다"));
        }
        if (c.average() == null) {
            errors.add(NodeConfigError.of("nodeConfig.average", "average는 필수입니다"));
        }

        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "해당 강의실에서 지원하지 않는 sensorType: " + c.measurementType()));
            return errors;
        }


        // windowSec 범위
        if (c.windowSec() == null) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "windowSec은 필수입니다"));
        } else if (c.windowSec() < 10 || c.windowSec() > 3600) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "windowSec 범위 초과 (10 ~ 3600): " + c.windowSec()));
        }

        return errors;
    }

}
