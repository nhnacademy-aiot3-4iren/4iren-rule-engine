package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record TemplateFlowCreateRequest(
        @NotBlank
        @Length(max = 50)
        String flowName,

        @Length(max = 255)
        String description,

        @NotEmpty
        List<TemplateNodeInfo> nodes,

        @NotNull
        List<TemplateConnectionInfo> connections
) {
}
