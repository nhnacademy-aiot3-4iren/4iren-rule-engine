package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "노드 설정 검증 요청")
public record NodeConfigValidateRequest (
        @Schema(description = "검증할 노드 타입별 설정 정보")
        NodeConfig nodeConfig
){
}
