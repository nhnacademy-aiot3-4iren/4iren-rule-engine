package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플로우 스케줄 생성 응답")
public record FlowScheduleCreateResponse(
        @Schema(description = "생성된 스케줄 ID", example = "1")
        Long scheduleId
) {
    public static FlowScheduleCreateResponse of(Long scheduleId) {
        return new FlowScheduleCreateResponse(scheduleId);
    }
}
