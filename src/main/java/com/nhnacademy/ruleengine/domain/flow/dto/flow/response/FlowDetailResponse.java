package com.nhnacademy.ruleengine.domain.flow.dto.flow.response;

import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionResponse;
import com.nhnacademy.ruleengine.domain.flow.dto.flowschedule.FlowScheduleResponse;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeResponse;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FlowDetailResponse (
        Long flowId,

        Long roomId,

        String flowName,

        String description,

        boolean isActive,

        boolean hasSchedule,

        List<NodeResponse> nodes,

        List<FlowScheduleResponse> schedules,

        List<ConnectionResponse> connections,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
){

    public static FlowDetailResponse from(
            Flow flow,
            boolean hasSchedule,
            List<FlowSchedule> schedules,
            List<Node> nodes,
            List<Connection> connections
    ){
        return FlowDetailResponse.builder()
                .flowId(flow.getId())
                .roomId(flow.getRoomId())
                .flowName(flow.getFlowName())
                .description(flow.getDescription())
                .isActive(flow.getIsActive())
                .hasSchedule(hasSchedule)
                .schedules(FlowScheduleResponse.fromList(schedules))
                .nodes(NodeResponse.fromList(nodes))
                .connections(ConnectionResponse.fromList(connections)).build();
    }
}
