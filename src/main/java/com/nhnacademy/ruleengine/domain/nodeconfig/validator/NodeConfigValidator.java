package com.nhnacademy.ruleengine.domain.nodeconfig.validator;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.SensorStaticMeta;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ValidationErrorResponse;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NodeConfigValidator {
    NodeType supportsNodeType();

    List<String> validate(NodeConfig nodeConfig, List<SensorStaticMeta> sensorStaticMetaList);
}
