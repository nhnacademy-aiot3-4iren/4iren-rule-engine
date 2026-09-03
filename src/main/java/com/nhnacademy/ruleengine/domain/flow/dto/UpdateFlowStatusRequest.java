package com.nhnacademy.ruleengine.domain.flow.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateFlowStatusRequest(
        @NotNull
        Boolean isActive
) {
}
