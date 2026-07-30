package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

public record NodeConfigUpdateRequest(
        @NonNull
        Long nodeId,

        @NotNull
        NodeType nodeType,

        @NotNull
        @Valid
        NodeConfig nodeConfig
) {
}
