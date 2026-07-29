package com.nhnacademy.ruleengine.domain.flow.dto.flow.request;

import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.flowschedule.FlowScheduleInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record FlowUpdateRequest(
        @NotNull
        Long flowId,
        @Length(max = 50)
        @NotBlank
        String flowName,
        @Length(max = 255)
        String description,

        @NotNull
        Boolean isActive,

        @NotEmpty
        List<NodeInfo> nodes,

        @NotNull
        List<ConnectionInfo> connections,

        @NotNull
        List<FlowScheduleInfo> schedules
) {

}

