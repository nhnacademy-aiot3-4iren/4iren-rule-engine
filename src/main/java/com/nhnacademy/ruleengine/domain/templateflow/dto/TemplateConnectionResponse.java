package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import lombok.Builder;

import java.util.List;

@Builder
public record TemplateConnectionResponse(
        Long sourceNodeId,
        Long targetNodeId,
        String branchType
) {
    public static List<TemplateConnectionResponse> fromList(List<Connection> connections) {
        return connections.stream()
                .map(c -> TemplateConnectionResponse.builder()
                        .sourceNodeId(c.getSourceNode().getId())
                        .targetNodeId(c.getTargetNode().getId())
                        .branchType(c.getBranchType())
                        .build()
                ).toList();
    }
}