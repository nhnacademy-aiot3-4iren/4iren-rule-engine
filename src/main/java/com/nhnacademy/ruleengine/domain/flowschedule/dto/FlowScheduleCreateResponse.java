package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import java.util.List;

public record FlowScheduleCreateResponse(
        List<Long> flowSchedulrIds
) {
    public static FlowScheduleCreateResponse of(List<Long> flowSchedulrIds) {
        return new FlowScheduleCreateResponse(flowSchedulrIds);
    }
}
