package com.nhnacademy.ruleengine.domain.flow.dto.flowschedule;

import com.nhnacademy.ruleengine.domain.flow.entity.FlowSchedule;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Builder
public record FlowScheduleResponse (
        Long scheduleId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
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
                    s -> {
                        return FlowScheduleResponse.builder()
                                .scheduleId(s.getId())
                                .dayOfWeek(s.getDayOfWeek())
                                .startTime(s.getStartTime())
                                .endTime(s.getEndTime()).build();
                    }
            ).toList();
    }
}
