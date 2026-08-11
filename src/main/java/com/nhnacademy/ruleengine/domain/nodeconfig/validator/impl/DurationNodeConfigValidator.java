package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
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
    public List<String> validate(NodeConfig nodeConfig, List<SensorStaticMeta> sensorStaticMetaList) {
        DurationNodeConfig c = (DurationNodeConfig) nodeConfig;
        List<String> errors = new ArrayList<>();

        SensorStaticMeta targetMeta = sensorStaticMetaList.stream()
                .filter(meta -> meta.measurementType() == c.measurementType())
                .findFirst()
                .orElse(null);

        if (targetMeta == null) {
            errors.add("해당 강의실에서 지원하지 않는 sensorType: " + c.measurementType());
            return errors;
        }

        // durationSec 범위 (최소 10초, 최대 24시간)
        if (c.durationSec() < 10 || c.durationSec() > 86400) {
            errors.add("durationSec 범위 초과 (10 ~ 86400): " + c.durationSec());
        }

        return errors;
    }
}
