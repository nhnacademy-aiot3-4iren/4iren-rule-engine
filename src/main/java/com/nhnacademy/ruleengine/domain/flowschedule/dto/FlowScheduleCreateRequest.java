package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FlowScheduleCreateRequest(
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
