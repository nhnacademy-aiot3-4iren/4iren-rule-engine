package com.nhnacademy.ruleengine.domain.templateflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "템플릿 플로우 수정 요청")
public record TemplateFlowUpdateRequest(
        @Schema(description = "템플릿 플로우 이름", example = "온도 알림 템플릿")
        @Size(max = 50)
        @NotBlank
        String flowName,

        @Schema(description = "템플릿 플로우 설명", example = "온도가 기준치를 넘으면 알림을 발송하는 템플릿")
        @Size(max = 255)
        String description,

        @Schema(description = "수정할 템플릿 노드 목록")
        @NotEmpty
        List<@Valid TemplateNodeInfo> nodes,

        @Schema(description = "수정할 템플릿 연결 목록")
        @NotEmpty
        List<@Valid TemplateConnectionInfo> connections
) {}
