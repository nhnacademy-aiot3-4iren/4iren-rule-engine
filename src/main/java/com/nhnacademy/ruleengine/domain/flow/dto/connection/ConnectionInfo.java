package com.nhnacademy.ruleengine.domain.flow.dto.connection;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import jakarta.validation.constraints.NotNull;


public record ConnectionInfo(
        //@NotNull 플로우 connections 수정에 사용한다면 nullable이 더 편하지 않을까? nodes, schedules 도 마찬가지
        Long connectionId,

        @NotNull
        Long flowId,

        @NotNull
        Long sourceNodeId,

        @NotNull
        Long targetNodeId,

        ConditionResult conditionResult
) {
}
