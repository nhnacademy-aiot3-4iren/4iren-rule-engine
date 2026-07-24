package com.nhnacademy.ruleengine.domain.flow.dto.flow.response;

import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionResponse;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeResponse;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
@Builder
public record TemplateDetailResponse (
        @NotNull
        Long templateId,
        @NotBlank
        String templateName,
        String description,
        @NotEmpty
        List<NodeResponse> nodes,
        @NotEmpty
        List<ConnectionResponse> connections
){
    public static TemplateDetailResponse from(
            Flow flowTempalte,
            List<Node> nodes,
            List<Connection> connections
    ) {
        return TemplateDetailResponse.builder()
                .templateId(flowTempalte.getId())
                .templateName(flowTempalte.getFlowName())
                .description(flowTempalte.getDescription())
                .nodes(NodeResponse.fromList(nodes))
                .connections(ConnectionResponse.fromList(connections)).build();
    }
}
