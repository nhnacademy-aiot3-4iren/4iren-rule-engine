package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import lombok.RequiredArgsConstructor;
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
    ){
        Map<Long, ExecutableFlow.ExecutableNode> nodeMap = buildNodeMap(nodes);
        Map<Long, List<Long>> adjacencyMap = buildAdjacencyMap(connections, nodeMap);
        Long startNodeId = findStartNodeId(nodeMap, connections);
        List<ExecutableFlow.ExecutableSchedule> executableSchedules = buildSchedules(flowSchedules);

        return new ExecutableFlow(
                flow.getId(),
                flow.getFlowName(),
                flow.getRoomId(),
                executableSchedules,
                startNodeId,
                nodeMap,
                adjacencyMap
        );
    }

    //
    private Map<Long, ExecutableFlow.ExecutableNode> buildNodeMap(List<Node> nodes){
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

    ////NodeId 별 다음 노드 Id 목록
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
    private Long findStartNodeId(
            Map<Long, ExecutableFlow.ExecutableNode> nodeMap,
            List<Connection> connections
    ) {
        Set<Long> hasIncomingEdge = connections.stream()
                .map(conn -> conn.getTargetNode().getId())
                .collect(Collectors.toSet());

        List<Long> startNodes = nodeMap.keySet().stream()
                .filter(id -> !hasIncomingEdge.contains(id))
                .toList();

        return startNodes.get(0);
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
