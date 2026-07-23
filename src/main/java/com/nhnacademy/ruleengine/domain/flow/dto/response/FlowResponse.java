package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;


public record FlowResponse(
        Long flowId,
        String flowName,
        String description,
        boolean hasSchedule,
        boolean isActive
) {

    static public FlowResponse from(Flow flow, boolean hasSchedule){
        return new FlowResponse(flow.getId(), flow.getFlowName(), flow.getDescription(), hasSchedule, flow.getIsActive());
    }

}