package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FlowScheduleInfo(
        Long scheduleId,
        @NotNull
        DayOfWeek dayOfWeek,
        @NotNull
        LocalTime startTime,
        @NotNull
        LocalTime endTime
) {
}
