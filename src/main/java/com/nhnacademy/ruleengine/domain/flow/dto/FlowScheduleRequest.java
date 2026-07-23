package com.nhnacademy.ruleengine.domain.flow.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FlowScheduleRequest(
        Long scheduleId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
