package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import lombok.Builder;

import java.util.List;

@Builder
public record NodeResponse (
        Long nodeId,

        String nodeName,

        NodeType nodeType,

        NodeConfig nodeConfig
){
    public static List<NodeResponse> fromList(
            List<Node> nodes
    ){
        return nodes.stream()
                .map(n -> NodeResponse.builder()
                        .nodeId(n.getId())
                        .nodeName(n.getNodeName())
                        .nodeType(n.getNodeType())
                        .nodeConfig(n.getNodeConfig())
                        .build()
                ).toList();
    }

}
