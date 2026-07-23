package com.nhnacademy.ruleengine.domain.flow.dto.request;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record FlowCreateRequest(
        @NotBlank
        String flowName,

        String description,

        @NotNull
        Boolean isActive,

        @NotNull
        List<FlowSchedule> schedules,

        @NotEmpty
        List<Node> nodes,

        @NotEmpty
        List<Connection> connections
) {

}
