package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import jakarta.validation.constraints.NotNull;


public record ConnectionInfo(
        @NotNull
        Long sourceNodeId,

        @NotNull
        Long targetNodeId
) {
}
