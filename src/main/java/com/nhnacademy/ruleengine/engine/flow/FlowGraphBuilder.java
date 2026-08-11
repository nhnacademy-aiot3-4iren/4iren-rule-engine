package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
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
            String branchString = conn.getBranchType();

            if ("FALSE".equalsIgnoreCase(branchString)) {
                falseAdjacencyMap.get(srcId).add(tgtId);
            } else {
                trueAdjacencyMap.get(srcId).add(tgtId);
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