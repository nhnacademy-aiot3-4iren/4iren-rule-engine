package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;

public record NodeRequest(
                Long nodeId,
                String nodeName,
                NodeType nodeType,
                String nodeConfig,
                int cooldownSec
) {
}
