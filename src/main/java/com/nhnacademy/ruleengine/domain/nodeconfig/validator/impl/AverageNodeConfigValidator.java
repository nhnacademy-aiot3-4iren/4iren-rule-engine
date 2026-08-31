package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
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
    public List<String> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        AverageNodeConfig c = (AverageNodeConfig) nodeConfig;
        List<String> errors = new ArrayList<>();

        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add("해당 강의실에서 지원하지 않는 sensorType: " + c.measurementType());
            return errors;
        }


        // windowSec 범위
        if (c.windowSec() < 10 || c.windowSec() > 3600) {
            errors.add("windowSec 범위 초과 (10 ~ 3600): " + c.windowSec());
        }

        return errors;
    }

}
