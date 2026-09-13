package com.nhnacademy.ruleengine.domain.templateflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "템플릿 플로우 생성 응답")
public record TemplateFlowCreateResponse(
        @Schema(description = "생성된 템플릿 플로우 ID", example = "1")
        Long templateFlowId
){
    public static TemplateFlowCreateResponse of(Long flowId){
        return new TemplateFlowCreateResponse(flowId);
    }

}
