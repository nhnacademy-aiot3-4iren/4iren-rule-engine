package com.nhnacademy.ruleengine.domain.flow.dto.node;

import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record NodeInfo(
//        @NotNull
        Long nodeId,
        @NotNull
        Long flowId,
        @NotBlank
        @Length(max = 50)
        String nodeName,
        @NotNull
        @Length(max = 20)
        NodeType nodeType,
        @NotNull
        String nodeConfig,

        int cooldownSec

) {
}
