package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FlowGraphBuilder {

    public ExecutableFlow build(
            Flow flow,
            List<Node> nodes,
            List<Connection> connections,
            List<FlowSchedule> flowSchedules
    ) {
        Map<Long, ExecutableFlow.ExecutableNode> nodeMap = buildNodeMap(nodes);

        Map<Long, List<Long>> trueAdjacencyMap = new HashMap<>();
        Map<Long, List<Long>> falseAdjacencyMap = new HashMap<>();

        nodeMap.keySet().forEach(id -> {
            trueAdjacencyMap.put(id, new ArrayList<>());
            falseAdjacencyMap.put(id, new ArrayList<>());
        });

        for (Connection conn : connections) {
            Long srcId = conn.getSourceNode().getId();
            Long tgtId = conn.getTargetNode().getId();
            BranchType branchType = parseBranchType(conn.getBranchType());

            switch (branchType) {
                case TRUE -> trueAdjacencyMap.get(srcId).add(tgtId);
                case FALSE -> falseAdjacencyMap.get(srcId).add(tgtId);
            }
        }

        Long startNodeId = findStartNodeId(nodeMap, connections);
        List<ExecutableFlow.ExecutableSchedule> executableSchedules = buildSchedules(flowSchedules);

        return new ExecutableFlow(
                flow.getId(),
                flow.getFlowName(),
                flow.getRoomId(),
                executableSchedules,
                startNodeId,
                nodeMap,
                trueAdjacencyMap,
                falseAdjacencyMap
        );
    }

    private BranchType parseBranchType(String branchType) {
        try {
            return BranchType.valueOf(branchType.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new InvalidFlowException();
        }
    }

    private Map<Long, ExecutableFlow.ExecutableNode> buildNodeMap(List<Node> nodes) {
        return nodes.stream()
                .collect(Collectors.toMap(
                        Node::getId,
                        node -> new ExecutableFlow.ExecutableNode(
                                node.getId(),
                                node.getNodeName(),
                                node.getNodeType(),
                                node.getNodeConfig(),
                                node.getCooldownSec()
                        )
                ));
    }

    //NodeId 별 다음 노드 Id 목록
    private Map<Long, List<Long>> buildAdjacencyMap(
            List<Connection> connections,
            Map<Long, ExecutableFlow.ExecutableNode> nodeMap
    ) {
        Map<Long, List<Long>> adjacencyMap = new HashMap<>();
        nodeMap.keySet().forEach(id -> adjacencyMap.put(id, new ArrayList<>()));

        for (Connection connection : connections) {
            adjacencyMap
                    .get(connection.getSourceNode().getId())
                    .add(connection.getTargetNode().getId());
        }

        return adjacencyMap;
    }

    private Long findStartNodeId(Map<Long, ExecutableFlow.ExecutableNode> nodeMap, List<Connection> connections) {
        Set<Long> hasIncomingEdge = connections.stream()
                .map(conn -> conn.getTargetNode().getId())
                .collect(Collectors.toSet());

        List<Long> startNodes = nodeMap.keySet().stream()
                .filter(id -> !hasIncomingEdge.contains(id))
                .toList();

        return startNodes.isEmpty() ? null : startNodes.getFirst();
    }

    private List<ExecutableFlow.ExecutableSchedule> buildSchedules(List<FlowSchedule> schedules) {
        return schedules.stream()
                .map(s -> new ExecutableFlow.ExecutableSchedule(
                        s.getDayOfWeek(),
                        s.getStartTime(),
                        s.getEndTime()
                ))
                .toList();
    }
}