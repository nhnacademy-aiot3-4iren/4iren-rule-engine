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

        // sensorType 존재 여부
        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add(NodeConfigError.of("nodeConfig.measurementType", "이 강의실에서 사용할 수 없는 측정 항목입니다: " + c.measurementType().getSensorDesc()));
            return errors;
        }


        // windowSec 범위 (최소 10초, 최대 1시간)
        if (c.windowSec() == null) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "계산에 사용할 시간을 설정해야 합니다."));
        } else if (c.windowSec() < 10 || c.windowSec() > 3600) {
            errors.add(NodeConfigError.of("nodeConfig.windowSec", "계산 시간은 10초 이상 1시간 이하로 설정해야 합니다. 현재값: " + c.windowSec() + "초"));
        }

        // gradient 0이면 의미 없음
        if (c.gradient() == null) {
            errors.add(NodeConfigError.of("nodeConfig.gradient", "기울기를 설정해야 합니다."));
        } else if (c.gradient() == 0) {
            errors.add(NodeConfigError.of("nodeConfig.gradient", "기울기는 0이 아닌 값으로 설정해야 합니다."));
        }

        return errors;
    }

}
