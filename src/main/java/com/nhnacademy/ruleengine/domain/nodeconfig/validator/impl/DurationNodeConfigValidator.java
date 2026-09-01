package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse.NodeConfigError;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.validator.NodeConfigValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DurationNodeConfigValidator implements NodeConfigValidator {
    @Override
    public NodeType supportsNodeType() {
        return NodeType.DURATION;
    }

    @Override
    public List<NodeConfigError> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        DurationNodeConfig c = (DurationNodeConfig) nodeConfig;
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
        if (c.threshold() == null) {
            errors.add(NodeConfigError.of("nodeConfig.threshold", "threshold는 필수입니다"));
        }

        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "해당 강의실에서 지원하지 않는 sensorType: " + c.measurementType()));
            return errors;
        }

        // durationSec 범위 (최소 10초, 최대 24시간)
        if (c.durationSec() == null) {
            errors.add(NodeConfigError.of("nodeConfig.durationSec", "durationSec은 필수입니다"));
        } else if (c.durationSec() < 10 || c.durationSec() > 86400) {
            errors.add(NodeConfigError.of("nodeConfig.durationSec", "durationSec 범위 초과 (10 ~ 86400): " + c.durationSec()));
        }

        return errors;
    }
}
