package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;

public record NodeInfo(
        String nodeName,
        NodeType nodeType,
        String nodeConfig,
        int cooldownSec

) {
}
