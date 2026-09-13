package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "템플릿 플로우 상세 응답")
public record TemplateDetailResponse (
        @Schema(description = "템플릿 플로우 ID", example = "1")
        Long templateId,

        @Schema(description = "템플릿 플로우 이름", example = "온도 알림 템플릿")
        String templateName,

        @Schema(description = "템플릿 플로우 설명", example = "온도 조건을 만족하면 알림을 발송하는 템플릿")
        String description,

        @Schema(description = "템플릿 노드 목록")
        List<TemplateNodeResponse> nodes,

        @Schema(description = "템플릿 연결 목록")
        List<TemplateConnectionResponse> connections
){
    public static TemplateDetailResponse from(
            Flow flow,
            List<Node> nodes,
            List<Connection> connections
    ){
        return TemplateDetailResponse.builder()
                .templateId(flow.getId())
                .templateName(flow.getFlowName())
                .description(flow.getDescription())
                .nodes(TemplateNodeResponse.fromList(nodes))
                .connections(TemplateConnectionResponse.fromList(connections))
                .build();
    }
}
