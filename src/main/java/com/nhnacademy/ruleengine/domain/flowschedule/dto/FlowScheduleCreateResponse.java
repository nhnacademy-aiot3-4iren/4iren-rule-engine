package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import lombok.Builder;

public record FlowScheduleCreateResponse(
        Long scheduleId
) {
    public static FlowScheduleCreateResponse of(Long scheduleId) {
        return new FlowScheduleCreateResponse(scheduleId);
    }
}
