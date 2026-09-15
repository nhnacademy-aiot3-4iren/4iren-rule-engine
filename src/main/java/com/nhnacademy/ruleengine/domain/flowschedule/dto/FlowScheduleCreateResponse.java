package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플로우 스케줄 생성 응답")
public record FlowScheduleCreateResponse(
        @Schema(description = "생성된 스케줄 ID 리스트", example = "[1, 2. 3]")
        List<Long> scheduleIds

) {
    public static FlowScheduleCreateResponse of(List<Long> scheduleIds) {
        return new FlowScheduleCreateResponse(scheduleIds);
    }
}
