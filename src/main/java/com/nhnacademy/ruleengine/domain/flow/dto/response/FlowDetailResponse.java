package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;

import java.util.List;

public record FlowDetailResponse (
        Long flowId,
        Long roomId,
        String flowName,
        String description,
        boolean isActive,
        boolean hasSchedule,
        List<FlowSchedule> schedules,
        List<Node> nodes,
        List<Connection> connections
){
    static FlowDetailResponse of(
            Long flowId,
            Long roomId,
            String flowName,
            String description,
            boolean isActive,
            boolean hasSchedule,
            List<FlowSchedule> schedules,
            List<Node> nodes,
            List<Connection> connections
    ){
        return new FlowDetailResponse(flowId, roomId, flowName, description, isActive,hasSchedule, schedules, nodes, connections);
    }
}
