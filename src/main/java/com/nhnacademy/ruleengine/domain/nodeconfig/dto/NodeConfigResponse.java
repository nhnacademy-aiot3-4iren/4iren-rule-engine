package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.validation.constraints.NotNull;

public record NodeConfigResponse(
        Long tempNodeId,
        NodeType nodeType,
        NodeConfig nodeConfig

) {
    public static NodeConfigResponse from(Node node) {
        return new NodeConfigResponse(
                node.getId(),
                node.getNodeType(),
                node.getNodeConfig()
        );
    }
}

