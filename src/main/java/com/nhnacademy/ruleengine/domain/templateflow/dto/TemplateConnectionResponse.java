package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "템플릿 플로우 연결 응답")
public record TemplateConnectionResponse(
        @Schema(description = "출발 노드 ID", example = "1")
        Long sourceNodeId,
        @Schema(description = "도착 노드 ID", example = "2")
        Long targetNodeId,
        @Schema(description = "조건 노드 분기 타입", example = "TRUE")
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
