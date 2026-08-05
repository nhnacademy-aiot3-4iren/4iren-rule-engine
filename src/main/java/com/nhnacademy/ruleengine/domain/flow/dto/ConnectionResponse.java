package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import lombok.Builder;

import java.util.List;

@Builder
public record ConnectionResponse (
        Long connectionId,

        Long flowId,

        Long sourceNodeId,

        Long targetNodeId,

        ConditionResult conditionResult
){
    public static List<ConnectionResponse> fromList(List<Connection> connections) {
        return connections.stream()
                .map(c -> ConnectionResponse.builder()
                        .connectionId(c.getId())
                        .flowId(c.getFlow().getId())
                        .sourceNodeId(c.getSourceNode().getId())
                        .targetNodeId(c.getTargetNode().getId())
                        .conditionResult(c.getConditionResult()).build()
                ).toList();
    }
}
