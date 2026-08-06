package com.nhnacademy.ruleengine.domain.templateflow.dto;


import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;

import java.util.List;

public record TemplateDetailResponse (
        Long templateId,

        String templateName,

        String description,

        List<TemplateNodeResponse> nodes,

        List<TemplateConnectionResponse> connections
){
    record TemplateNodeResponse(
            Long nodeId,

            String nodeName,

            NodeType nodeType,

            NodeConfig nodeConfig,

            int cooldownSec
    ){}
    record TemplateConnectionResponse(
            Long connectionId,

            Long flowId,

            Long sourceNodeId,

            Long targetNodeId,

            ConditionResult conditionResult
    ){}

}