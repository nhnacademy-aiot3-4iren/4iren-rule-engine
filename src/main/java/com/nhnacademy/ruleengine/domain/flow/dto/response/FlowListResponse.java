package com.nhnacademy.ruleengine.domain.flow.dto.response;

import java.util.List;

public record FlowListResponse(
        List<FlowResponse> flowResponseList
) {
    static FlowListResponse of(
            List<FlowResponse> flowResponseList
    ){
        return new FlowListResponse(flowResponseList);
    }
}
