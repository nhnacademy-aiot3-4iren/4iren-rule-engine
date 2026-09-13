package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "플로우 스케줄 생성 요청")
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
