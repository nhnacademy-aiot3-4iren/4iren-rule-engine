package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;

import java.util.List;

public record TemplateDetailResponse (
        Long templateId,
        String templateName,
        String description,
        List<Node> nodes,
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
