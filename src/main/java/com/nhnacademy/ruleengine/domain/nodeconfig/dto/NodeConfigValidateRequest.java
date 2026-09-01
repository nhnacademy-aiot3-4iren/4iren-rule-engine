package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;

public record NodeConfigValidateRequest (
        NodeConfig nodeConfig
){
}
