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
        @NotNull
        Long flowId,
        @NotNull
        Long roomId,
        @Length(max = 50)
        @NotBlank
        String flowName,
        @Length(max = 255)
        String description,
        @NotNull
        boolean isActive,
        @NotNull
        boolean hasSchedule,
        @NotEmpty
        List<NodeResponse> nodes,
        @NotNull
        List<FlowScheduleResponse> schedules,
        @NotNull
        List<ConnectionResponse> connections,
        @NotNull
        LocalDateTime createdAt,
        @NotNull
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
                .connections(ConnectionResponse.fromList(connections))
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt()).build();
    }
}
