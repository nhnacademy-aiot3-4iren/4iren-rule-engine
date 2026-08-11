package com.nhnacademy.ruleengine.domain.templateflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record TemplateFlowUpdateRequest(
        @Length(max = 50)
        @NotBlank
        String flowName,

        @Length(max = 255)
        String description,

        @NotEmpty
        List<@Valid TemplateNodeInfo> nodes,

        @NotNull
        List<@Valid TemplateConnectionInfo> connections
) {}