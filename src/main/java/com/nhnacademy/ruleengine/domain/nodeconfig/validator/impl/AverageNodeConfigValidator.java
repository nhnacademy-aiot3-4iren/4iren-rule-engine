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

        if (c.x() == null) {
            errors.add("x 좌표는 필수입니다");
        }
        if (c.y() == null) {
            errors.add("y 좌표는 필수입니다");
        }
        if (c.measurementType() == null) {
            errors.add("measurementType은 필수입니다");
            return errors;
        }
        if (c.operator() == null) {
            errors.add("operator는 필수입니다");
        }
        if (c.unit() == null || c.unit().isBlank()) {
            errors.add("unit은 필수입니다");
        }
        if (c.average() == null) {
            errors.add("average는 필수입니다");
        }

        SensorMetaInfo targetMeta = sensorMetaInfoList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add("해당 강의실에서 지원하지 않는 sensorType: " + c.measurementType());
            return errors;
        }


        // windowSec 범위
        if (c.windowSec() == null) {
            errors.add("windowSec은 필수입니다");
        } else if (c.windowSec() < 10 || c.windowSec() > 3600) {
            errors.add("windowSec 범위 초과 (10 ~ 3600): " + c.windowSec());
        }

        return errors;
    }

}
