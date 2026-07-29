package com.nhnacademy.ruleengine.domain.flow.dto.flow.response;

import jakarta.validation.constraints.NotNull;

public record FlowCreateResponse (
        Long flowId
){
    public static FlowCreateResponse of(Long flowId){
        return new FlowCreateResponse(flowId);
    }
}
