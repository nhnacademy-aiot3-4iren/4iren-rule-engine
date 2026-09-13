package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "플로우 연결 응답")
public record ConnectionResponse (
        @Schema(description = "연결 ID", example = "1")
        Long connectionId,
        @Schema(description = "출발 노드 ID", example = "1")
        Long sourceNodeId,
        @Schema(description = "도착 노드 ID", example = "2")
        Long targetNodeId,
        @Schema(description = "조건 노드 분기 타입", example = "TRUE")
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
