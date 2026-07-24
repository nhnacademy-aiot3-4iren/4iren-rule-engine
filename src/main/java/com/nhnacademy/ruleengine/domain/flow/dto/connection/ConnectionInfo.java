package com.nhnacademy.ruleengine.domain.flow.dto.connection;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;


public record ConnectionInfo(
        Long flowId,
        Long sourceNodeId,
        Long targetNodeId,
        ConditionResult conditionResult
) {
}
