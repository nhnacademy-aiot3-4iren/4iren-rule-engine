package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TemplateDetailResponse (
        @NotNull
        Long templateId,
        @NotBlank
        String templateName,
        String description,
        @NotEmpty
        List<Node> nodes,
        @NotEmpty
        List<Connection> connections
){
    static TemplateDetailResponse of(
            Long templateId,
            String templateName,
            String description,
            List<Node> nodes,
            List<Connection> connections
    ) {
        return new TemplateDetailResponse(templateId, templateName, description, nodes, connections);
    }
}
