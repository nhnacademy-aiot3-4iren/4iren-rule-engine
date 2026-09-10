package com.nhnacademy.ruleengine.domain.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "플로우 상세 응답")
public record FlowDetailResponse (
        @Schema(description = "플로우 ID", example = "1")
        Long flowId,

        @Schema(description = "강의실 ID", example = "1")
        Long roomId,

        @Schema(description = "플로우 이름", example = "강의실 온도 알림")
        String flowName,

        @Schema(description = "플로우 설명", example = "온도가 기준치를 넘으면 알림을 발송한다.")
        String description,

        @Schema(description = "플로우 활성화 여부", example = "true")
        Boolean isActive,

        @Schema(description = "플로우 노드 목록")
        List<NodeResponse> nodes,

        @Schema(description = "플로우 연결 목록")
        List<ConnectionResponse> connections,

        @Schema(description = "강의실에서 사용할 수 있는 센서 측정 메타 정보 목록")
        List<SensorMetaInfo> sensorMetaInfos,

        @Schema(description = "생성 일시", example = "2026-09-10T09:00:00.000000")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime createdAt,

        @Schema(description = "수정 일시", example = "2026-09-10T10:00:00.000000")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime updatedAt
){

    public static FlowDetailResponse from(
            Flow flow,
            List<Node> nodes,
            List<Connection> connections,
            List<SensorMetaInfo> sensorMetaInfos
    ){
        return FlowDetailResponse.builder()
                .flowId(flow.getId())
                .roomId(flow.getRoomId())
                .flowName(flow.getFlowName())
                .description(flow.getDescription())
                .isActive(flow.getIsActive())
                .nodes(NodeResponse.fromList(nodes))
                .connections(ConnectionResponse.fromList(connections))
                .sensorMetaInfos(sensorMetaInfos)
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt()).build();
    }
}
