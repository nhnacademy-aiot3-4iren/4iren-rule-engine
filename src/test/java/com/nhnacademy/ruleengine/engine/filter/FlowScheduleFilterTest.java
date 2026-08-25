package com.nhnacademy.ruleengine.engine.filter;

import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlowScheduleFilterTest {

    private FlowScheduleFilter filter;

    private final LocalDateTime baseTime = LocalDateTime.of(2026, 8, 25, 10, 0); // 2026년 8월 25일은 화요일 10시

    @BeforeEach
    void sesrUp(){
        filter = new FlowScheduleFilter();
    }

    //helper
    private ExecutableFlow.ExecutableSchedule createSchedule(DayOfWeek day, String start, String end){
        return new ExecutableFlow.ExecutableSchedule(day, LocalTime.parse(start), LocalTime.parse(end));
    }

    private ExecutableFlow createMockFlow(List<ExecutableFlow.ExecutableSchedule> schedules){
        ExecutableFlow flow= mock(ExecutableFlow.class);
        when(flow.schedules()).thenReturn(schedules);
        when(flow.flowId()).thenReturn(1L);
        return flow;
    }

    @Test
    @DisplayName("스케줄 리스트가 null -> 실행 가능")
    void null_schedule_returns_true(){
        ExecutableFlow flow = createMockFlow(null);
        boolean result = filter.isSchedulable(flow);
        assertTrue(result);
    }

    @Test
    @DisplayName("스케줄 리스트가 isEmpty -> 실행 가능")
    void empty_schedule_returns_true(){
        ExecutableFlow flow = createMockFlow(List.of());
        boolean result = filter.isSchedulable(flow);
        assertTrue(result);
    }


    @Test
    @DisplayName("요일과 시간이 모두 일치하면 true 반환")
    void matched_schedule_returns_true(){
        ExecutableFlow.ExecutableSchedule schedule =  createSchedule(DayOfWeek.TUESDAY, "09:00:00", "11:00:00");
        ExecutableFlow flow =createMockFlow(List.of(schedule));

        boolean result = filter.isSchedulable(flow, baseTime);

        assertTrue(result);
    }

    @Test
    @DisplayName("요일이 다르면 false 반환")
    void mismatched_dayOfWeek_returns_false(){
        ExecutableFlow.ExecutableSchedule schedule =  createSchedule(DayOfWeek.MONDAY, "09:00", "11:00");
        ExecutableFlow flow = createMockFlow(List.of(schedule));

        boolean result = filter.isSchedulable(flow, baseTime);

        assertFalse(result);
    }

    @Test
    @DisplayName("자정을 넘어가는 시간이면 false 반환 - start 보다 end 시간이 앞서는경우")
    void startTime_isAfter_endtime_returns_false(){
        ExecutableFlow.ExecutableSchedule schedule = createSchedule(DayOfWeek.TUESDAY, "18:00", "02:00");
        ExecutableFlow flow = createMockFlow(List.of(schedule));

        boolean result = filter.isSchedulable(flow, baseTime);

        assertFalse(result);
    }
}
