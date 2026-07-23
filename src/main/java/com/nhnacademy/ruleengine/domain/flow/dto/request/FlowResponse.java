package com.nhnacademy.ruleengine.domain.flow.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FlowResponse (
        @NotNull
        Long flowId,
        @NotBlank
        String flowName,

        String description,

        @NotNull
        boolean hasSchedule,
        @NotNull
        boolean isActive
){

}
