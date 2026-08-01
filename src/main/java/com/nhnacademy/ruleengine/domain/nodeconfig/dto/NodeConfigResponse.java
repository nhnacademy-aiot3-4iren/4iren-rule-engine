package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;

import java.util.List;

public record NodeConfigResponse(
        Long nodeId,
        NodeConfig nodeConfig,
        List<SensorStaticMeta> sensorStaticMetaList
) {
//    public static NodeConfigResponse from(Node node, SensorStaticMeta sensorStaticMeta) {
//        return new NodeConfigResponse(
//                node.getId(),
//                node.getNodeConfig()
//        );
//    }
}