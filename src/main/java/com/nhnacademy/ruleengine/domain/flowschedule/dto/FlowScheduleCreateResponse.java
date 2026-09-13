package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import java.util.List;

public record FlowScheduleCreateResponse(
        List<Long> scheduleIds
) {
    public static FlowScheduleCreateResponse of(List<Long> scheduleIds) {
        return new FlowScheduleCreateResponse(scheduleIds);
    }
}
