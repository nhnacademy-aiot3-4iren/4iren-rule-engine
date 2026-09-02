package com.nhnacademy.ruleengine.domain.flow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FlowCreateRequest(
        @NotBlank
        @Size(max = 50)
        String flowName,

        @Size(max = 255)
        String description,

        @NotNull
        Boolean isActive,

        @NotEmpty
        List<@Valid NodeInfo> nodes,

        @NotEmpty
        List<@Valid ConnectionInfo> connections
) {
}
