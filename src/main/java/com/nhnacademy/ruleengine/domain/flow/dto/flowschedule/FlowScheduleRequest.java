package com.nhnacademy.ruleengine.domain.flow.dto.flowschedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FlowScheduleRequest(
        @NotNull
        DayOfWeek dayOfWeek,
        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime startTime,
        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime endTime
) {
}
