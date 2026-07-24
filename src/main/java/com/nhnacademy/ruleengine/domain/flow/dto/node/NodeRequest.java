package com.nhnacademy.ruleengine.domain.flow.dto.node;

import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record NodeRequest(
        @Length(max = 50)
        String nodeName,
        @Length(max = 20)
        NodeType nodeType,
        @NotNull
        String nodeConfig,
        int cooldownSec
) {
}
