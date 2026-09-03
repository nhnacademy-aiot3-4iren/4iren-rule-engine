package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
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

//    @NotNull
//    AlertChannel channel,

    @NotBlank
    String alertTitle,

    @NotNull
    AlertType alertType,

    @NotNull
    @Positive
    Integer dedupWindowSec
) implements NodeConfig {

}
