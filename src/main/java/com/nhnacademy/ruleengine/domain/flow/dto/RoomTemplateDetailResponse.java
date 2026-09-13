package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
@Builder
@Schema(description = "강의실별 추천 템플릿 플로우 상세 응답")
public record RoomTemplateDetailResponse(

        @Schema(description = "템플릿 플로우 이름", example = "온도 알림 템플릿")
        String templateName,

        @Schema(description = "템플릿 플로우 설명", example = "온도 조건을 만족하면 알림을 발송하는 템플릿")
        String description,

        @Schema(description = "템플릿 노드 목록")
        List<NodeResponse> nodes,

        @Schema(description = "템플릿 연결 목록")
        List<ConnectionResponse> connections,

        @Schema(description = "강의실에서 사용할 수 있는 센서 측정 메타 정보 목록")
        List<SensorMetaInfo> sensorMetaInfos
){
    public static RoomTemplateDetailResponse from(
            Flow flowTemplate,
            List<Node> nodes,
            List<Connection> connections,
            List<SensorMetaInfo> sensorMetaInfos
    ) {
        return RoomTemplateDetailResponse.builder()
                .templateName(flowTemplate.getFlowName())
                .description(flowTemplate.getDescription())
                .nodes(NodeResponse.fromList(nodes))
                .connections(ConnectionResponse.fromList(connections))
                .sensorMetaInfos(sensorMetaInfos).build();
    }
}
