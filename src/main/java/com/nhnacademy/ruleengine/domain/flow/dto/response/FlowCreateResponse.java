package com.nhnacademy.ruleengine.domain.flow.dto.response;

import jakarta.validation.constraints.NotNull;

public record FlowCreateResponse (
        @NotNull
        Long flowId
){
    public static FlowCreateResponse of(Long flowId){
        return new FlowCreateResponse(flowId);
    }
}
