package com.nhnacademy.ruleengine.domain.flow.dto.flow.request;

import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.flowschedule.FlowScheduleInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.flowschedule.FlowScheduleRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record FlowCreateRequest(
        @NotBlank
        @Length(max = 50)
        String flowName,

        @Length(max = 255)
        String description,

        @NotNull
        Boolean isActive,

        @NotNull
        List<FlowScheduleRequest> schedules,

        @NotEmpty
        List<NodeRequest> nodes,

        @NotNull
        List<ConnectionRequest> connections
) {

}
