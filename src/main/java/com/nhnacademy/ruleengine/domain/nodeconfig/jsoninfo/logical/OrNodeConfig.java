package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.logical;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "OR 논리 노드 설정")
public record OrNodeConfig(
        @Schema(description = "노드 타입", example = "OR")
        @NotNull
        NodeType nodeType,

        @Schema(description = "플로우 편집 화면의 x 좌표", example = "500")
        @NotNull
        Integer x,

        @Schema(description = "플로우 편집 화면의 y 좌표", example = "120")
        @NotNull
        Integer y
) implements NodeConfig {
}
