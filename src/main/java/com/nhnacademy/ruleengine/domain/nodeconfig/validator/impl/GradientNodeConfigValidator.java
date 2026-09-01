package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse.NodeConfigError;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GradientNodeConfigValidator implements NodeConfigValidator {
    @Override
    public NodeType supportsNodeType() {
        return NodeType.GRADIENT;
    }

    @Override
    public List<NodeConfigError> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        GradientNodeConfig c = (GradientNodeConfig) nodeConfig;
        List<NodeConfigError> errors = new ArrayList<>();

        if (c.x() == null) {
            errors.add(NodeConfigError.of("nodeConfig.x", "x 좌표는 필수입니다"));
        }
        if (c.y() == null) {
            errors.add(NodeConfigError.of("nodeConfig.y", "y 좌표는 필수입니다"));
        }
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

        // sensorType 존재 여부
        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "해당 강의실에서 지원하지 않는 sensorType: " + c.measurementType()));
            return errors;
        }


        // windowSec 범위 (최소 10초, 최대 1시간)
        if (c.windowSec() == null) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "windowSec은 필수입니다"));
        } else if (c.windowSec() < 10 || c.windowSec() > 3600) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "windowSec 범위 초과 (10 ~ 3600): " + c.windowSec()));
        }

        // gradient 0이면 의미 없음
        if (c.gradient() == null) {
            errors.add(NodeConfigError.of("nodeConfig.gradient", "gradient 값은 필수입니다"));
        } else if (c.gradient() == 0) {
            errors.add(NodeConfigError.of("nodeConfig.gradient", "gradient 값은 0이 될 수 없습니다"));
        }

        return errors;
    }

}
