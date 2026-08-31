package com.nhnacademy.ruleengine.domain.nodeconfig.validator;

import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;

import java.util.List;

public interface NodeConfigValidator {
    NodeType supportsNodeType();

    List<String> validate(NodeConfig nodeConfig, List<SensorMetaInfo> sensorMetaInfoList);
}
