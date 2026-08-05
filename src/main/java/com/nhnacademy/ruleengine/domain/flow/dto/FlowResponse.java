package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FlowResponse(
        Long flowId,

        String flowName,

        String description,

        boolean hasSchedule,

        boolean isActive,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {

    static public FlowResponse from(Flow flow, boolean hasSchedule){
        return FlowResponse.builder()
                .flowId(flow.getId())
                .flowName(flow.getFlowName())
                .description(flow.getDescription())
                .hasSchedule(hasSchedule)
                .isActive(flow.getIsActive())
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt()).build();

    }

}