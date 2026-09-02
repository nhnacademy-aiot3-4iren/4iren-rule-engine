package com.nhnacademy.ruleengine.domain.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record FlowResponse(
        Long flowId,

        String flowName,

        String description,

        boolean isActive,

        Long scheduleCount,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime updatedAt
) {

    public static  FlowResponse from(Flow flow){
        return FlowResponse.builder()
                .flowId(flow.getId())
                .flowName(flow.getFlowName())
                .description(flow.getDescription())
                .isActive(flow.getIsActive())
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt()).build();

    }
    public static List<FlowResponse> fromList(List<Flow> flowList, Map<Long, Long> scheduleCountMap){
         return flowList.stream()
                .map(flow -> FlowResponse.builder()
                        .flowId(flow.getId())
                        .flowName(flow.getFlowName())
                        .description(flow.getDescription())
                        .isActive(flow.getIsActive())
                        .scheduleCount(scheduleCountMap.getOrDefault(flow.getId(), 0L))
                        .createdAt(flow.getCreatedAt())
                        .updatedAt(flow.getUpdatedAt()).build()
                ).toList();
    }

}