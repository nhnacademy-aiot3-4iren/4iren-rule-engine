package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import lombok.Builder;

import java.util.List;
@Builder
public record NodeConfigResponse(
        Long nodeId,
        NodeConfig nodeConfig,
        List<SensorStaticMeta> sensorStaticMetaList
) {
    public static NodeConfigResponse of(Long nodeId, NodeConfig nodeConfig, List<SensorStaticMeta> sensorStaticMetaList) {
        return NodeConfigResponse.builder().nodeId(nodeId).nodeConfig(nodeConfig).sensorStaticMetaList(sensorStaticMetaList).build();
    }

}