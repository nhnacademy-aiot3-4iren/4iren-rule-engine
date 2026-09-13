package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.start;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "시작 노드 설정")
public record StartNodeConfig(
        @Schema(description = "노드 타입", example = "START")
        @NotNull
        NodeType nodeType,

        @Schema(description = "플로우 편집 화면의 x 좌표", example = "100")
        @NotNull
        Integer x,

        @Schema(description = "플로우 편집 화면의 y 좌표", example = "120")
        @NotNull
        Integer y
) implements NodeConfig {

}
