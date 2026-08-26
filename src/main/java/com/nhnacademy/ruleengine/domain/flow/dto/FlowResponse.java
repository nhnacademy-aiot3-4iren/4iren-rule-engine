package com.nhnacademy.ruleengine.domain.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FlowResponse(
        Long flowId,

        String flowName,

        String description,

        boolean isActive,

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
    public static List<FlowResponse> fromList(List<Flow> flowList){
         return flowList.stream()
                .map(flow -> FlowResponse.builder()
                        .flowId(flow.getId())
                        .flowName(flow.getFlowName())
                        .description(flow.getDescription())
                        .isActive(flow.getIsActive())
                        .createdAt(flow.getCreatedAt())
                        .updatedAt(flow.getUpdatedAt()).build()
                ).toList();
    }

}