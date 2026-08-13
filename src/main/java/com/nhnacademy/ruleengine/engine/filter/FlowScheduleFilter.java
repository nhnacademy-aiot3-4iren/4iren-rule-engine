package com.nhnacademy.ruleengine.engine.filter;

import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
public class FlowScheduleFilter {

    public boolean isSchedulable(ExecutableFlow flow) {
        return isSchedulable(flow, LocalDateTime.now());
    }

    public boolean isSchedulable(ExecutableFlow flow, LocalDateTime now) {
        if (flow.schedules() == null || flow.schedules().isEmpty()) {
            return true;
        }

        boolean matched = flow.schedules().stream()
                .anyMatch(schedule -> matches(schedule, now.getDayOfWeek(), now.toLocalTime()));

        if (!matched) {
            log.debug("flowId: {} - 등록된 스케줄과 현재 시각({}) 불일치", flow.flowId(), now);
        }
        return matched;
    }

    private boolean matches(ExecutableFlow.ExecutableSchedule schedule, DayOfWeek day, LocalTime time) {
        if (!schedule.dayOfWeek().equals(day)) {
            return false;
        }
        if (schedule.startTime().isAfter(schedule.endTime())) {
            return !time.isBefore(schedule.startTime()) || time.isBefore(schedule.endTime());
        }
        return !time.isBefore(schedule.startTime()) && time.isBefore(schedule.endTime());
    }
}
