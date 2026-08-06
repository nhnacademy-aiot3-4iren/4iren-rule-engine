package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.dto.FlowCreateResponse;

public record TemplateCreateResponse(
        Long templateFlowId
){
    public static TemplateCreateResponse of(Long flowId){
        return new TemplateCreateResponse(flowId);
    }

}