package com.nhnacademy.ruleengine.domain.flow.dto;

import java.util.List;

public record FlowListResponse(

        List<FlowResponse> flowResponseList
) {
    public static FlowListResponse of(
            List<FlowResponse> flowResponseList
    ){
        return new FlowListResponse(flowResponseList);
    }
}
