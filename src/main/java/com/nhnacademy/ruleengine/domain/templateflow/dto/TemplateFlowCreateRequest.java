package com.nhnacademy.ruleengine.domain.templateflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TemplateFlowCreateRequest(
        @NotBlank
        @Size(max = 50)
        String flowName,

        @Size(max = 255)
        String description,

        @NotEmpty
        List<@Valid TemplateNodeInfo> nodes,

        @NotNull
        List<@Valid TemplateConnectionInfo> connections
) {

}
