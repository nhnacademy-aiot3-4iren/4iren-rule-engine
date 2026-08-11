package com.nhnacademy.ruleengine.domain.flowschedule.dto;

public record FlowScheduleCreateResponse(
        Long scheduleId
) {
    public static FlowScheduleCreateResponse of(Long scheduleId) {
        return new FlowScheduleCreateResponse(scheduleId);
    }
}
