package com.nhnacademy.ruleengine.domain.flow.dto.flow.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FlowRequest(
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
