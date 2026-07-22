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

public record FlowCreateRequest(
        String flowName,
        String description,
        Boolean isActive,
        List<FlowSchedule> schedules,
        List<Node> nodes,
        List<Connection> connections
) {
//    public record NodeCreateRequest(
//            String tempNodeKey,
//            NodeType nodeType,
//            String nodeName,
//            String nodeConfig,
//            Integer cooldownSec
//    ) {}
//
//    public record ConnectionCreateRequest(
//            String sourceTempNodeKey,
//            String targetTempNodeKey,
//            ConditionResult conditionResult
//    ) {}
//
//    public record ScheduleCreateRequest(
//            DayOfWeek dayOfWeek,
//            LocalTime startTime,
//            LocalTime endTime
//    ) {}

}
