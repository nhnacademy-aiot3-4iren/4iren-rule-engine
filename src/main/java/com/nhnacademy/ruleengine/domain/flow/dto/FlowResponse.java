package com.nhnacademy.ruleengine.domain.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Builder
public record FlowResponse(
        Long flowId,

        String flowName,

        String description,

        boolean isActive,

        Long scheduleCount,

        String createdAt,


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