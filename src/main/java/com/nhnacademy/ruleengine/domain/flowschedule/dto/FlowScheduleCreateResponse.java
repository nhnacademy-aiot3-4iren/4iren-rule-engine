package com.nhnacademy.ruleengine.domain.flowschedule.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플로우 스케줄 생성 응답")
public record FlowScheduleCreateResponse(
        @Schema(description = "생성된 스케줄 ID 리스트", example = "1")
        List<Long> scheduleIds

) {
    public static FlowScheduleCreateResponse of(List<Long> scheduleIds) {
        return new FlowScheduleCreateResponse(scheduleIds);
    }
}
