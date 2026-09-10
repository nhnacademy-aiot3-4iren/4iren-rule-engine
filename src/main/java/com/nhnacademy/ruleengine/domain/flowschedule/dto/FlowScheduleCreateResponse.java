package com.nhnacademy.ruleengine.domain.flowschedule.dto;

public record FlowScheduleCreateResponse(
        Long flowId
) {
    public static FlowScheduleCreateResponse of(Long flowId) {
        return new FlowScheduleCreateResponse(flowId);
    }
}
