package com.nhnacademy.ruleengine.domain.flow.dto.flow;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record NodeInfo(
        @NotNull
        Long nodeId,//양수: 기존에 있던 노드, 음수: 새로 생성된 노드의 임시 아이디 -> 재할당

        @NotBlank
        @Length(max = 50)
        String nodeName,

        @NotNull
        @Length(max = 20)
        NodeType nodeType,

        @NotNull
        NodeConfig nodeConfig,

        Integer cooldownSec



) {
        public boolean isNew() {
                return nodeId < 0;
        }
}
