package com.nhnacademy.ruleengine.domain.flow.dto.response;

public record FlowCreateResponse (
        Long flowId
){
    public static FlowCreateResponse of(Long flowId){
        return new FlowCreateResponse(flowId);
    }
}
