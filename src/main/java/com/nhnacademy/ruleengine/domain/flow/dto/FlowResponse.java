package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Builder
@Schema(description = "플로우 요약 응답")
public record FlowResponse(
        @Schema(description = "플로우 ID", example = "1")
        Long flowId,

        @Schema(description = "플로우 이름", example = "강의실 온도 알림")
        String flowName,

        @Schema(description = "플로우 설명", example = "온도가 기준치를 넘으면 알림을 발송한다.")
        String description,

        @Schema(description = "플로우 활성화 여부", example = "true")
        boolean isActive,

        @Schema(description = "등록된 스케줄 개수", example = "2")
        Long scheduleCount,

        @Schema(description = "생성 일시", example = "2026-09-10T09:00:00.000000")
        String createdAt,


        @Schema(description = "수정 일시", example = "2026-09-10T10:00:00.000000")
        String updatedAt
) {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    public static List<FlowResponse> fromList(List<Flow> flowList, Map<Long, Long> scheduleCountMap){
         return flowList.stream()
                .map(flow -> FlowResponse.builder()
                        .flowId(flow.getId())
                        .flowName(flow.getFlowName())
                        .description(flow.getDescription())
                        .isActive(flow.getIsActive())
                        .scheduleCount(scheduleCountMap.getOrDefault(flow.getId(), 0L))
                        .createdAt(flow.getCreatedAt() != null? flow.getCreatedAt().format(ISO_FORMATTER):null)
                        .updatedAt(flow.getUpdatedAt() != null? flow.getUpdatedAt().format(ISO_FORMATTER):null).build()
                ).toList();
    }

}
