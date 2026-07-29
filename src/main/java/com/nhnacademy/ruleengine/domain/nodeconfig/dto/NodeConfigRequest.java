package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;

public record NodeConfigRequest(
        NodeType nodeType
) {
}
