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
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "측정 항목을 선택해야 합니다."));
            return errors;
        }
        if (c.operator() == null) {
            errors.add(NodeConfigError.of("nodeConfig.operator", "비교 조건을 선택해야 합니다."));
        }
        if (c.unit() == null || c.unit().isBlank()) {
            errors.add(NodeConfigError.of("nodeConfig.unit", "단위를 입력해야 합니다."));
        }
        if (c.average() == null) {
            errors.add(NodeConfigError.of("nodeConfig.average", "평균 기준값을 설정해야 합니다."));
        }

        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "이 강의실에서 사용할 수 없는 측정 항목입니다: " + c.measurementType().getSensorDesc()));
            return errors;
        }


        // windowSec 범위
        if (c.windowSec() == null) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "평균 측정 시간을 설정해야 합니다."));
        } else if (c.windowSec() < 10 || c.windowSec() > 3600) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "계산 시간은 10초 이상 1시간 이하로 설정해야 합니다. 현재값: " + c.windowSec() + "초"));
        }

        return errors;
    }

}
