package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Builder
public record ExecutableFlow(
        Long flowId,
        String flowName,
        Long roomId,
        List<ExecutableSchedule> schedules,

        Long startNodeId,
        Map<Long, ExecutableNode> nodeMap,
        // NodeId -> True Outgoing Target Node IDs
        Map<Long, List<Long>> trueAdjacencyMap,
        // NodeId -> False Outgoing Target Node IDs
        Map<Long, List<Long>> falseAdjacencyMap
) {
    public record ExecutableNode(
            Long nodeId,
            String nodeName,
            NodeType nodeType,
            NodeConfig nodeConfig,
            Integer cooldownSec
    ) {}

    public record ExecutableSchedule(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {}
}