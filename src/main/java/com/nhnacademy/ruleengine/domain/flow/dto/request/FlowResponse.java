package com.nhnacademy.ruleengine.domain.flow.dto.request;


public record FlowResponse (
        Long flowId,
        String flowName,
        String description,
        boolean hasSchedule,
        boolean isActive
){

}
