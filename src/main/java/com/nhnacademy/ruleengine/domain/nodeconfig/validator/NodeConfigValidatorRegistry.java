package com.nhnacademy.ruleengine.domain.nodeconfig.validator;

import com.nhnacademy.ruleengine.common.exception.notfound.NodeTypeNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.dto.SensorMetaInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NodeConfigValidatorRegistry {
    private final List<NodeConfigValidator> validators;
    private final Map<NodeType, NodeConfigValidator> registry = new HashMap<>();

    @PostConstruct
    public void init(){
        validators.forEach(v -> registry.put(v.supportsNodeType(), v));
    }

    public List<String> validate(NodeType nodeType, NodeConfig nodeconfig, List<SensorMetaInfo> sensorMetaInfoList){
        NodeConfigValidator nodeConfigValidator = registry.get(nodeType);
        if(nodeConfigValidator == null){
            throw new NodeTypeNotFoundException();
        }
        return nodeConfigValidator.validate(nodeconfig, sensorMetaInfoList);
    }
}
