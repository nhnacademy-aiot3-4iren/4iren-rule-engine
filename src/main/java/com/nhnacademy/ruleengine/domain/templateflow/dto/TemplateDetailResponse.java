package com.nhnacademy.ruleengine.domain.templateflow.dto;


import com.nhnacademy.ruleengine.domain.flow.dto.ConnectionResponse;
import com.nhnacademy.ruleengine.domain.flow.dto.NodeResponse;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import lombok.Builder;

import java.util.List;

@Builder
public record TemplateDetailResponse (
        Long templateId,

        String templateName,

        String description,

        List<TemplateNodeResponse> nodes,

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