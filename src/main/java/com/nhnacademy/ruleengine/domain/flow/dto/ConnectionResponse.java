package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import lombok.Builder;

import java.util.List;

@Builder
public record ConnectionResponse (
        Long connectionId,
        Long sourceNodeId,
        Long targetNodeId,
        BranchType branchType
) {
    public static List<ConnectionResponse> fromList(List<Connection> connections) {
        return connections.stream()
                .map(c -> ConnectionResponse.builder()
                        .connectionId(c.getId())
                        .sourceNodeId(c.getSourceNode().getId())
                        .targetNodeId(c.getTargetNode().getId())
                        .branchType(BranchType.valueOf(c.getBranchType()))
                        .build()
                ).toList();
    }
}