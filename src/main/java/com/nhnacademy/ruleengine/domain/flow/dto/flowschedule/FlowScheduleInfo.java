package com.nhnacademy.ruleengine.domain.flow.dto.flowschedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FlowScheduleInfo(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {

}
