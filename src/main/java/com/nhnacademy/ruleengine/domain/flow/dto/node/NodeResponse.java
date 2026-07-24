package com.nhnacademy.ruleengine.domain.flow.dto.node;

import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Builder
public record NodeResponse (
        @NotNull
        Long nodeId,
        @Length(max = 50)
        @NotNull
        String nodeName,
        @Length(max = 20)
        @NotNull
        NodeType nodeType,
        @NotNull
        String nodeConfig,

        int cooldownSec
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
                        .cooldownSec(n.getCooldownSec()).build()
                ).toList();
    }

}
