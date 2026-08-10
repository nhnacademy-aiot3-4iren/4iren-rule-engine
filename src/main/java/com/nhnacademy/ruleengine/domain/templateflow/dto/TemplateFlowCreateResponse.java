package com.nhnacademy.ruleengine.domain.templateflow.dto;

public record TemplateFlowCreateResponse(
        Long templateFlowId
){
    public static TemplateFlowCreateResponse of(Long flowId){
        return new TemplateFlowCreateResponse(flowId);
    }

}