package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "템플릿 플로우 노드 응답")
public record TemplateNodeResponse(
        @Schema(description = "노드 ID", example = "1")
        Long nodeId,

        @Schema(description = "노드 이름", example = "온도 임계치 판단")
        String nodeName,

        @Schema(description = "노드 타입", example = "THRESHOLD")
        NodeType nodeType,

        @Schema(description = "노드 타입별 설정 정보")
        NodeConfig nodeConfig
){
    public static List<TemplateNodeResponse> fromList(
            List<Node> nodes
    ){
        return nodes.stream()
                .map(n -> TemplateNodeResponse.builder()
                        .nodeId(n.getId())
                        .nodeName(n.getNodeName())
                        .nodeType(n.getNodeType())
                        .nodeConfig(n.getNodeConfig())
                        .build()
                ).toList();
    }
}
