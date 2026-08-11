package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FlowDetailResponse (
        Long flowId,

        Long roomId,

        String flowName,

        String description,

        boolean isActive,

        List<NodeResponse> nodes,

        List<ConnectionResponse> connections,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
){

    public static FlowDetailResponse from(
            Flow flow,
            List<Node> nodes,
            List<Connection> connections
    ){
        return FlowDetailResponse.builder()
                .flowId(flow.getId())
                .roomId(flow.getRoomId())
                .flowName(flow.getFlowName())
                .description(flow.getDescription())
                .isActive(flow.getIsActive())
                .nodes(NodeResponse.fromList(nodes))
                .connections(ConnectionResponse.fromList(connections))
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt()).build();
    }
}
