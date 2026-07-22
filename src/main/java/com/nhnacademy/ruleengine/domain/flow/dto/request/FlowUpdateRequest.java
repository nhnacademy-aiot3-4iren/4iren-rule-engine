package com.nhnacademy.ruleengine.domain.flow.dto.request;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record FlowUpdateRequest(
        String name,
        String description,
        Boolean isActive,
        List<Node> nodes,
        List<Connection> connections,
        List<FlowSchedule> schedules
) {

}

