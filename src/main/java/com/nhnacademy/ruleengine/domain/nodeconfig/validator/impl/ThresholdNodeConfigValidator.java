package com.nhnacademy.ruleengine.domain.nodeconfig.validator.impl;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
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
    public List<String> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList) {
        ThresholdNodeConfig thresholdNodeConfig = (ThresholdNodeConfig) nodeConfig;
        List<String > errors = new ArrayList<>();

        if (thresholdNodeConfig.x() == null) {
            errors.add("x 좌표는 필수입니다");
        }
        if (thresholdNodeConfig.y() == null) {
            errors.add("y 좌표는 필수입니다");
        }
        if (thresholdNodeConfig.measurementType() == null) {
            errors.add("measurementType은 필수입니다");
            return errors;
        }
        if (thresholdNodeConfig.operator() == null) {
            errors.add("operator는 필수입니다");
        }
        if (thresholdNodeConfig.unit() == null || thresholdNodeConfig.unit().isBlank()) {
            errors.add("unit은 필수입니다");
        }
        if(thresholdNodeConfig.threshold() == null){
            errors.add("임계값 설정은 필수잆니다.");
        }

        boolean validMeasurementType = sensorMetaInfoList.stream()
                .anyMatch(meta -> meta.measurementType() == thresholdNodeConfig.measurementType());
        if (!validMeasurementType) {
            errors.add("해당 강의실에서 지원하지 않는 measurementType: " + thresholdNodeConfig.measurementType());
            return errors; // 이후 검증 의미 없음
        }

        return errors;
    }
    
}
