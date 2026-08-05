package com.nhnacademy.ruleengine.domain.templateflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record TemplateFlowUpdateRequest(
        @NotNull
        Long flowId,

        @Length(max = 50)
        @NotBlank
        String flowName,

        @Length(max = 255)
        String description,

        @NotNull
        Boolean isActive,

        @NotEmpty
        List<TemplateNodeInfo> nodes,

        @NotNull
        List<TemplateConnectionInfo> connections
) {}