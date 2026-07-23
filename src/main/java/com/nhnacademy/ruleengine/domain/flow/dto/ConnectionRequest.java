package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;

public record ConnectionRequest (
        Long connectionId,
        Long flowId,
        Long sourceNodeId,
        ConditionResult conditionResult
){

}
