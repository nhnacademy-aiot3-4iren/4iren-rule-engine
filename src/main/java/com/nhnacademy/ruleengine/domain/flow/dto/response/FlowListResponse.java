package com.nhnacademy.ruleengine.domain.flow.dto.response;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FlowListResponse(
        @NotNull
        List<FlowResponse> flowResponseList
) {
    static FlowListResponse of(
            List<FlowResponse> flowResponseList
    ){
        return new FlowListResponse(flowResponseList);
    }
}
