package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "플로우 스케줄 목록 조회 응답")
public record FlowScheduleListResponse(
        @Schema(description = "플로우 ID", example = "1")
        Long flowId,
        @Schema(description = "플로우 스케줄 목록")
        List<FlowScheduleResponse> schedules
) {

    public static FlowScheduleListResponse from(Long flowId, List<FlowSchedule> scheduleList){
        return FlowScheduleListResponse.builder().flowId(flowId).schedules(FlowScheduleResponse.fromList(scheduleList)).build();
    }
}
