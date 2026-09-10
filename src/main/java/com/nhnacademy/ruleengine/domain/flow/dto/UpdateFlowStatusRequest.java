package com.nhnacademy.ruleengine.domain.flow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "플로우 활성화 상태 변경 요청")
public record UpdateFlowStatusRequest(
        @Schema(description = "변경할 활성화 여부", example = "true")
        @NotNull
        Boolean isActive
) {
}
