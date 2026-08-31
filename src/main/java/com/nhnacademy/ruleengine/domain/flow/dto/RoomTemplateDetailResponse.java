package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import lombok.Builder;

import java.util.List;
@Builder
public record RoomTemplateDetailResponse(

        String templateName,

        String description,

        List<NodeResponse> nodes,

        List<ConnectionResponse> connections,

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
