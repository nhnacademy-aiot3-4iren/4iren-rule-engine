package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import jakarta.validation.constraints.NotNull;

public 	record TemplateConnectionInfo(
        @NotNull
        Long sourceNodeId,

        @NotNull
        Long targetNodeId
) {}
