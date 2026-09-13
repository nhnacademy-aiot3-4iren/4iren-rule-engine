package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;


@Schema(description = "플로우 노드 연결 정보")
public record ConnectionInfo(
        @Schema(description = "출발 노드 ID", example = "-1")
        @NotNull
        Long sourceNodeId,

        @Schema(description = "도착 노드 ID", example = "-2")
        @NotNull
        Long targetNodeId,

        @Schema(description = "조건 노드 분기 타입. 값이 없으면 TRUE로 처리된다.", example = "TRUE")
        BranchType branchType
) {
        public ConnectionInfo {
                if (branchType == null) {
                        branchType = BranchType.TRUE;
                }
        }
}
