package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record FlowScheduleCreateRequest(
        @NotEmpty
        List<@NotNull @Valid FlowScheduleRequest> flowScheduleRequestList
) {
        public record FlowScheduleRequest(

                @NotNull
                DayOfWeek dayOfWeek,

                @NotNull
                @JsonFormat(pattern = "HH:mm:ss")
                LocalTime startTime,

                @NotNull
                @JsonFormat(pattern = "HH:mm:ss")
                LocalTime endTime
        ){}
}
