package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record FlowResponse(
        @NotNull
        Long flowId,
        @NotBlank
        String flowName,

        String description,
        @NotNull
        boolean hasSchedule,
        @NotNull
        boolean isActive
) {

    static public FlowResponse from(Flow flow, boolean hasSchedule){
        return new FlowResponse(flow.getId(), flow.getFlowName(), flow.getDescription(), hasSchedule, flow.getIsActive());
    }

}