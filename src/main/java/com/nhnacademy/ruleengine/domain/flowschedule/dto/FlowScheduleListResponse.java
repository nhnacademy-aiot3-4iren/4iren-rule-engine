package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import lombok.Builder;

import java.util.List;

@Builder
public record FlowScheduleListResponse(
        Long flowId,
        List<FlowScheduleResponse> schedules
) {

    public static FlowScheduleListResponse from(Long flowId, List<FlowSchedule> scheduleList){
        return FlowScheduleListResponse.builder().flowId(flowId).schedules(FlowScheduleResponse.fromList(scheduleList)).build();
    }
}
