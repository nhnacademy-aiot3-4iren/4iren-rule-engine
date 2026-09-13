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
        @Schema(description = "생성할 플로우 스케줄 목록")
        @NotEmpty
        List<@NotNull @Valid FlowScheduleRequest> flowScheduleRequestList
) {
        @Schema(description = "개별 플로우 스케줄 항목")
        public record FlowScheduleRequest(
                @Schema(description = "실행 요일", example = "MONDAY")
                @NotNull
                DayOfWeek dayOfWeek,

                @Schema(description = "시작 시간 (HH:mm:ss)", example = "09:00:00", type = "string")
                @NotNull
                @JsonFormat(pattern = "HH:mm:ss")
                LocalTime startTime,

                @Schema(description = "종료 시간 (HH:mm:ss)", example = "18:00:00", type = "string")
                @NotNull
                @JsonFormat(pattern = "HH:mm:ss")
                LocalTime endTime
        ){}
}
