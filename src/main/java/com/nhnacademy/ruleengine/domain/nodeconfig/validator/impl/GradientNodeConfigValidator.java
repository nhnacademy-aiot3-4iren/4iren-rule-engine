package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
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
    public List<String> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        GradientNodeConfig c = (GradientNodeConfig) nodeConfig;
        List<String> errors = new ArrayList<>();

        // sensorType 존재 여부
        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add("해당 강의실에서 지원하지 않는 sensorType: " + c.measurementType());
            return errors;
        }


        // windowSec 범위 (최소 10초, 최대 1시간)
        if (c.windowSec() < 10 || c.windowSec() > 3600) {
            errors.add("windowSec 범위 초과 (10 ~ 3600): " + c.windowSec());
        }

        // gradient 0이면 의미 없음
        if (c.gradient() == 0) {
            errors.add("gradient 값은 0이 될 수 없습니다");
        }

        return errors;
    }

}
