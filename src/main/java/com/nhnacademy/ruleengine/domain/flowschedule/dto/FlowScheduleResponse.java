package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Builder
@Schema(description = "플로우 스케줄 응답")
public record FlowScheduleResponse (
        @Schema(description = "스케줄 ID", example = "1")
        Long scheduleId,

        @Schema(description = "실행 요일", example = "MONDAY")
        DayOfWeek dayOfWeek,

        @Schema(description = "스케줄 시작 시간", example = "09:00:00")
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime startTime,

        @Schema(description = "스케줄 종료 시간", example = "18:00:00")
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime endTime
){
    public static FlowScheduleResponse from(FlowSchedule schedule){
        return FlowScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime()).build();
    }
    public static List<FlowScheduleResponse> fromList(List<FlowSchedule> schedules){
            if(schedules == null) {return List.of();}

            return schedules.stream().map(
                    s -> FlowScheduleResponse.builder()
                            .scheduleId(s.getId())
                            .dayOfWeek(s.getDayOfWeek())
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime()).build()
            ).toList();
    }
}
