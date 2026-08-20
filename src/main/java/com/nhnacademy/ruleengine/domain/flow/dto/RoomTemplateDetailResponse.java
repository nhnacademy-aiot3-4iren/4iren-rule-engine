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

        List<ConnectionResponse> connections
){
    public static RoomTemplateDetailResponse from(
            Flow flowTemplete,
            List<Node> nodes,
            List<Connection> connections
    ) {
        return RoomTemplateDetailResponse.builder()
                .templateName(flowTemplete.getFlowName())
                .description(flowTemplete.getDescription())
                .nodes(NodeResponse.fromList(nodes))
                .connections(ConnectionResponse.fromList(connections)).build();
    }
}
