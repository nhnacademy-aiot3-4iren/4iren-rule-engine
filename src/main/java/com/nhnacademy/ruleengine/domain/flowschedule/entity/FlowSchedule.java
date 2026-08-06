package com.nhnacademy.ruleengine.domain.flowschedule.entity;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateRequest;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "flow_schedules")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlowSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false)
    private Flow flow;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Builder
    public FlowSchedule(Flow flow, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.flow = flow;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static FlowSchedule create(Flow flow, FlowScheduleCreateRequest request){
        return FlowSchedule.builder()
                .flow(flow).dayOfWeek(request.dayOfWeek()).startTime(request.startTime()).endTime(request.endTime()).build();
    }

}
