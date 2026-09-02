package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.logical;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import jakarta.validation.constraints.NotNull;

public record OrNodeConfig(
        @NotNull
        NodeType nodeType,

        @NotNull
        Integer x,

        @NotNull
        Integer y
) implements NodeConfig {
}
