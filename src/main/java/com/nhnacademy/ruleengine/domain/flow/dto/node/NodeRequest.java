package com.nhnacademy.ruleengine.domain.flow.dto.node;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record NodeRequest(
        @NotNull
        Long tempNodeId,

        @Length(max = 50)
        String nodeName,
        @Length(max = 20)
        NodeType nodeType,
        @NotNull
        NodeConfig nodeConfig,
        int cooldownSec
) {
}
