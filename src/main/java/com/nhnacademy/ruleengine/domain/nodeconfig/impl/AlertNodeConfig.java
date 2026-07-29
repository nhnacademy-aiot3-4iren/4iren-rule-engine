package com.nhnacademy.ruleengine.domain.nodeconfig.impl;

import com.nhnacademy.ruleengine.common.enums.AlertChannel;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlertNodeConfig(

    @NotNull
    NodeType nodeType,

    @NotNull
    Integer x,

    @NotNull
    Integer y,

    @NotNull
    AlertChannel channel,

    @NotBlank
    String chatid,

    @NotBlank
    String messageTemplate,

    @Positive
    Integer dedupWindowSec
) implements NodeConfig {
}
