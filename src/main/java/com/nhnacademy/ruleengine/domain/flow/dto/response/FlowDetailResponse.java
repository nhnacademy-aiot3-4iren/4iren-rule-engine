package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FlowDetailResponse (
        @NotNull
        Long flowId,
        @NotNull
        Long roomId,
        @NotBlank
        String flowName,
        String description,
        @NotNull
        boolean isActive,
        @NotNull
        boolean hasSchedule,

        @NotNull
        List<FlowSchedule> schedules,
        @NotEmpty
        List<Node> nodes,
        @Negative
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
