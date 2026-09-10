package com.nhnacademy.ruleengine.domain.flow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플로우 생성 요청")
public record FlowCreateRequest(
        @Schema(description = "플로우 이름", example = "강의실 온도 알림")
        @NotBlank
        @Size(max = 50)
        String flowName,

        @Schema(description = "플로우 설명", example = "온도가 기준치를 넘으면 알림을 발송한다.")
        @Size(max = 255)
        String description,

        @Schema(description = "플로우 활성화 여부", example = "true")
        @NotNull
        Boolean isActive,

        @Schema(description = "플로우를 구성하는 노드 목록")
        @NotEmpty
        List<@Valid NodeInfo> nodes,

        @Schema(description = "노드 간 연결 목록")
        @NotEmpty
        List<@Valid ConnectionInfo> connections
) {
}
