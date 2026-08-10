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
    Map<Long ,ExecutableNode> nodeMap,
    //NodeId -> 다음 노드 Id 목록
    Map<Long, List<Long>> adjacencyMap
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
    ){}

}
