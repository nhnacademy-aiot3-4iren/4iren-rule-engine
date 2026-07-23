package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import lombok.Data;
import lombok.Getter;


public record ConnectionInfo(
        Long flowId,
        Long sourceNodeId,
        Long targetNodeId,
        ConditionResult conditionResult
) {
}
