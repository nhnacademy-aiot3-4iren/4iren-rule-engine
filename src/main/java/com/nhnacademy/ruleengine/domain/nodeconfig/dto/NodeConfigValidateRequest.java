package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import jakarta.validation.constraints.NotNull;

public record NodeConfigValidateRequest (
        @NotNull
        NodeConfig nodeConfig
){
}
