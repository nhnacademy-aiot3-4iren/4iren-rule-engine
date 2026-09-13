package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "플로우 노드 정보")
public record NodeInfo(
        @Schema(description = "노드 ID. 신규 노드는 임시 음수 ID를 사용하고 저장 시 실제 ID로 재할당된다.", example = "-1")
        @NotNull
        Long nodeId,//양수: 기존에 있던 노드, 음수: 새로 생성된 노드의 임시 아이디 -> 재할당

        @Schema(description = "노드 이름", example = "온도 임계치 판단")
        @NotBlank
        @Size(max = 50)
        String nodeName,

        @Schema(description = "노드 타입", example = "THRESHOLD")
        @NotNull
        NodeType nodeType,

        @Schema(description = "노드 타입별 설정 정보")
        @NotNull
        NodeConfig nodeConfig
) {
        public boolean isNew() {
                return nodeId < 0;
        }
}
