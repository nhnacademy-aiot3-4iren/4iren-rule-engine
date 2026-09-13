package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(description = "플로우 스케줄 생성 요청")
public record FlowScheduleCreateRequest(
        @Schema(description = "실행 요일", example = "MONDAY")
        @NotNull
        DayOfWeek dayOfWeek,

        @Schema(description = "스케줄 시작 시간", example = "09:00:00")
        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime startTime,

        @Schema(description = "스케줄 종료 시간", example = "18:00:00")
        @NotNull
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime endTime
) {
}
