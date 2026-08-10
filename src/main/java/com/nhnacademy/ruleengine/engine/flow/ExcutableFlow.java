package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public record ExcutableFlow(
    Long flowId,
    String flowName,
    Long roomId,
    List<ExcutableFlowSchedule> schedules,

    Long startNodeId,
    Map<Long ,ExcutableNode> nodeMap,
    //NodeId -> 다음 노드 Id 목록
    Map<Long, List<Long>> adjacencyMap
) {
    record ExcutableNode(
        Long nodeId,
        String nodeName,
        NodeType nodeType,
        NodeConfig nodeConfig,
        Integer cooldownSec

    ) {}
    record ExcutableFlowSchedule(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
    ){}
}
