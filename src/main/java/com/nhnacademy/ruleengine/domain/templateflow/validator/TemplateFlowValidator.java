package com.nhnacademy.ruleengine.domain.templateflow.validator;

import com.nhnacademy.ruleengine.common.advice.ValidationErrorResponse;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateConnectionInfo;
import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateNodeInfo;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.stream.Collectors;

@Component
public class TemplateFlowValidator {
    // 템플릿 플로우의 노드, 연결, 기본 노드 설정 구조를 검증한다.
    public void validate( List<TemplateNodeInfo> nodes,  List<TemplateConnectionInfo> connections){
        List<ValidationErrorResponse.ValidationError> errors =  new ArrayList<>();

        validateRequestShape(nodes, connections, errors);
        if(!errors.isEmpty()){
            throw new FlowValidationFailed(errors);
        }

        Map<Long, TemplateNodeInfo> nodeMap = nodes.stream()
                .filter(node -> node.nodeId() != null)
                .collect(Collectors.toMap(
                        TemplateNodeInfo::nodeId,
                        node -> node,
                        (first, second) -> first
                ));
        Map<Long, Degree> degrees = calculateDegrees(nodes, connections);

        validateNodeIds(nodes, errors);
        validateNodeCount(nodes, errors);
        validateRequiredNodeTypes(nodes, errors);
        validateNodeConfig(nodes, errors);
        validateConnectionReferences(connections, nodeMap, errors);
        validatePortRules(nodes, connections, degrees, errors);
        validateNoIsolatedNode(nodes, connections, errors);
        validateSingleEntryPoint(nodes,connections, errors);
        validateNoCycle(nodes, connections, errors);

        if(!errors.isEmpty()){
            throw new FlowValidationFailed(errors);
        }
    }

    // nodes, connections 자체가 null인지 먼저 확인해 이후 검증에서 NPE가 발생하지 않게 한다.
    private void validateRequestShape(
            List<TemplateNodeInfo> nodes,
            List<TemplateConnectionInfo> connections,
            List<ValidationErrorResponse.ValidationError> errors
    ) {
        if (nodes == null) {
            errors.add(ValidationErrorResponse.ValidationError.of("nodes", "nodes는 필수입니다."));
        }
        if (connections == null) {
            errors.add(ValidationErrorResponse.ValidationError.of("connections", "connections는 필수입니다."));
        }
    }

    // START, 판단 노드, 행동 노드를 포함할 수 있도록 최소 노드 개수를 검증한다.
    private void validateNodeCount( List<TemplateNodeInfo> nodes, List<ValidationErrorResponse.ValidationError> errors){
        if(nodes == null || nodes.size() < 3){
            errors.add(ValidationErrorResponse.ValidationError.of("nodes", "노드는 최소 3개 이상이어야 합니다. START, 판단 노드, 행동 노드가 각각 1개 이상 필요합니다."));
        }
    }

    // 필수 노드 타입인 START, 판단 노드, 행동 노드의 존재 조건을 검증한다.
    private void validateRequiredNodeTypes(List<TemplateNodeInfo> nodes, List<ValidationErrorResponse.ValidationError> errors) {
        long startNodeCount = nodes.stream()
                .filter(node -> node.nodeType() == NodeType.START)
                .count();
        boolean hasConditionNode = nodes.stream()
                .anyMatch(node -> node.nodeType() != null && node.nodeType().isConditionNode());
        boolean hasActionNode = nodes.stream()
                .anyMatch(node -> node.nodeType() != null && node.nodeType().isActionNode());

        if (startNodeCount != 1) {
            errors.add(ValidationErrorResponse.ValidationError.of("nodes", "START 노드는 정확히 1개여야 합니다. 현재: " + startNodeCount + "개"));
        }
        if (!hasConditionNode) {
            errors.add(ValidationErrorResponse.ValidationError.of("nodes", "판단 노드가 최소 1개 이상 필요합니다."));
        }
        if (!hasActionNode) {
            errors.add(ValidationErrorResponse.ValidationError.of("nodes", "행동 노드가 최소 1개 이상 필요합니다."));
        }
    }

    // 요청 안에서 같은 nodeId가 중복 사용되었는지 검증한다.
    private void validateNodeIds(List<TemplateNodeInfo> nodes, List<ValidationErrorResponse.ValidationError> errors) {
        Set<Long> seen = new HashSet<>();
        nodes.stream()
                .map(TemplateNodeInfo::nodeId)
                .filter(Objects::nonNull)
                .filter(id -> !seen.add(id))
                .forEach(id -> errors.add(ValidationErrorResponse.ValidationError.of(id, "nodeId", "중복된 nodeId입니다.")));
    }

    // 템플릿 노드의 nodeConfig 필수값과 nodeType 일치 여부를 검증한다.
    private void validateNodeConfig(List<TemplateNodeInfo> nodes, List<ValidationErrorResponse.ValidationError> errors) {
        for (TemplateNodeInfo node : nodes) {
            if (node.nodeConfig() == null) {
                errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "nodeConfig", "nodeConfig는 필수입니다."));
                continue;
            }

            NodeConfig nodeConfig = node.nodeConfig();
            if (nodeConfig.nodeType() == null) {
                errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "nodeConfig.nodeType", "nodeConfig.nodeType은 필수입니다."));
                continue;
            }
            if (node.nodeType() != nodeConfig.nodeType()) {
                errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "nodeConfig.nodeType", "nodeType과 nodeConfig.nodeType이 일치하지 않습니다."));
            }
        }
    }

    // connection이 존재하는 노드를 참조하는지, 자기 자신/중복 연결이 없는지 검증한다.
    private void validateConnectionReferences(
            List<TemplateConnectionInfo> connections,
            Map<Long, TemplateNodeInfo> nodeMap,
            List<ValidationErrorResponse.ValidationError> errors
    ) {
        Set<String> seenConnections = new HashSet<>();
        for (TemplateConnectionInfo connection : connections) {
            if (connection.sourceNodeId() == null || connection.targetNodeId() == null) {
                continue;
            }

            if (!nodeMap.containsKey(connection.sourceNodeId())) {
                errors.add(ValidationErrorResponse.ValidationError.of(connection.sourceNodeId(), "sourceNodeId", "존재하지 않는 sourceNodeId입니다."));
            }
            if (!nodeMap.containsKey(connection.targetNodeId())) {
                errors.add(ValidationErrorResponse.ValidationError.of(connection.targetNodeId(), "targetNodeId", "존재하지 않는 targetNodeId입니다."));
            }
            if (Objects.equals(connection.sourceNodeId(), connection.targetNodeId())) {
                errors.add(ValidationErrorResponse.ValidationError.of(connection.sourceNodeId(), "connection", "자기 자신으로 연결할 수 없습니다."));
            }

            String key = connection.sourceNodeId() + "->" + connection.targetNodeId() + ":" + connection.branchType();
            if (!seenConnections.add(key)) {
                errors.add(ValidationErrorResponse.ValidationError.of(connection.sourceNodeId(), "connection", "중복된 connection입니다."));
            }
        }
    }

    // 노드 타입별 입력/출력 포트 개수와 연결 방향, branchType 규칙을 검증한다.
    private void validatePortRules(
            List<TemplateNodeInfo> nodes,
            List<TemplateConnectionInfo> connections,
            Map<Long, Degree> degrees,
            List<ValidationErrorResponse.ValidationError> errors
    ) {
        for (TemplateNodeInfo node : nodes) {
            Degree degree = degrees.getOrDefault(node.nodeId(), new Degree());
            NodeType nodeType = node.nodeType();
            if (nodeType == null) {
                continue;
            }

            if (nodeType == NodeType.START) {
                if (degree.incoming != 0) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "incoming", "START 노드는 입력을 가질 수 없습니다."));
                }
                if (degree.outgoing == 0) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "outgoing", "START 노드는 출력이 최소 1개 필요합니다."));
                }
            } else if (nodeType.isConditionNode()) {
                if (degree.incoming != 1) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "incoming", "판단 노드는 입력이 정확히 1개여야 합니다. 현재: " + degree.incoming + "개"));
                }
                if (degree.outgoing == 0) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "outgoing", "판단 노드는 출력이 최소 1개 필요합니다."));
                }
            } else if (nodeType == NodeType.OR) {
                if (degree.incoming < 1) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "incoming", "OR 노드는 입력이 최소 1개 필요합니다."));
                }
                if (degree.outgoing == 0) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "outgoing", "OR 노드는 출력이 최소 1개 필요합니다."));
                }
            } else if (nodeType.isActionNode()) {
                if (degree.incoming != 1) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "incoming", "행동 노드는 입력이 정확히 1개여야 합니다. 현재: " + degree.incoming + "개"));
                }
                if (degree.outgoing != 0) {
                    errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "outgoing", "행동 노드는 출력을 가질 수 없습니다."));
                }
            }
        }

        Map<Long, NodeType> nodeTypeById = nodes.stream()
                .filter(node -> node.nodeId() != null)
                .collect(Collectors.toMap(TemplateNodeInfo::nodeId, TemplateNodeInfo::nodeType, (first, second) -> first));

        for (TemplateConnectionInfo connection : connections) {
            NodeType sourceType = nodeTypeById.get(connection.sourceNodeId());
            NodeType targetType = nodeTypeById.get(connection.targetNodeId());
            if (sourceType == NodeType.START && connection.branchType() != BranchType.TRUE) {
                errors.add(ValidationErrorResponse.ValidationError.of(connection.sourceNodeId(), "branchType", "START 노드의 출력 connection은 TRUE만 사용할 수 있습니다."));
            }
            if (sourceType != null && sourceType.isActionNode()) {
                errors.add(ValidationErrorResponse.ValidationError.of(connection.sourceNodeId(), "sourceNodeId", "행동 노드는 connection의 source가 될 수 없습니다."));
            }
            if (targetType == NodeType.START) {
                errors.add(ValidationErrorResponse.ValidationError.of(connection.targetNodeId(), "targetNodeId", "START 노드는 connection의 target이 될 수 없습니다."));
            }
        }
    }

    // 고립 노드가 존재하는지 검증한다.
    private void validateNoIsolatedNode( List<TemplateNodeInfo> nodes, List<TemplateConnectionInfo> connections, List<ValidationErrorResponse.ValidationError> errors){
        Set<Long> connectionNodeIds = new HashSet<>();
        connections.forEach(
                conn ->{
                    connectionNodeIds.add(conn.sourceNodeId());
                    connectionNodeIds.add(conn.targetNodeId());
                }
        );

        nodes.stream()
                .filter(node -> !connectionNodeIds.contains(node.nodeId()))
                .forEach(node -> errors.add(ValidationErrorResponse.ValidationError.of(node.nodeId(), "nodes", "연결되지 않은 고립 노드가 있습니다: " + node.nodeName())));
    }

    // incoming connection이 없는 시작 지점이 정확히 하나인지 검증한다.
    private void validateSingleEntryPoint(List<TemplateNodeInfo> nodes, List<TemplateConnectionInfo> connections, List<ValidationErrorResponse.ValidationError> errors) {
        Set<Long> hasIncoming = connections.stream()
                .map(TemplateConnectionInfo::targetNodeId)
                .collect(Collectors.toSet());

        long startNodeCount = nodes.stream()
                .filter(node -> !hasIncoming.contains(node.nodeId()))
                .count();

        if(startNodeCount == 0){
            errors.add(ValidationErrorResponse.ValidationError.of("nodes", "시작 노드가 없습니다. 순환 연결이 의심됩니다."));
        } else if (startNodeCount > 1) {
            errors.add(ValidationErrorResponse.ValidationError.of("nodes", "시작노드는 1개여야 합니다. 현재: " + startNodeCount + "개"));
        }
    }


    // 연결 그래프를 DFS로 순회해 순환 연결이 존재하는지 검증한다.
    private void validateNoCycle( List<TemplateNodeInfo> nodes, List<TemplateConnectionInfo> connections, List<ValidationErrorResponse.ValidationError> errors) {
        Map<Long, List<Long>> adjacency = new HashMap<>();
        nodes.forEach(node -> adjacency.put(node.nodeId(), new ArrayList<>()));
        connections.forEach(
                conn -> adjacency
                        .computeIfAbsent(conn.sourceNodeId(), k ->new ArrayList<>())
                        .add(conn.targetNodeId())
        );

        Set<Long> visited  = new HashSet<>();
        Set<Long> inStack = new HashSet<>();

        for(Long nodeId : adjacency.keySet()){
            if(hasCycle(nodeId, adjacency, visited, inStack)){
                errors.add(ValidationErrorResponse.ValidationError.of("connections", "순환 연결이 감지되었습니다."));
                return;
            }
        }
    }

    // DFS 방문 상태를 기준으로 현재 경로 안에서 같은 노드를 다시 만나는지 확인한다.
    private boolean hasCycle(Long nodeId, Map<Long, List<Long>> adjacency, Set<Long> visited, Set<Long> inStack){
        if(inStack.contains(nodeId)){
            return true;
        }
        if(visited.contains(nodeId)){
            return false;
        }
        visited.add(nodeId);
        inStack.add(nodeId);

        for(Long next : adjacency.getOrDefault(nodeId, List.of())){
            if(hasCycle(next, adjacency, visited, inStack)) {
                return true;
            }
        }
        inStack.remove(nodeId);
        return false;

    }

    // 각 노드의 incoming/outgoing connection 개수를 계산한다.
    private Map<Long, Degree> calculateDegrees(List<TemplateNodeInfo> nodes, List<TemplateConnectionInfo> connections) {
        Map<Long, Degree> degrees = new HashMap<>();
        nodes.stream()
                .map(TemplateNodeInfo::nodeId)
                .filter(Objects::nonNull)
                .forEach(id -> degrees.put(id, new Degree()));

        for (TemplateConnectionInfo connection : connections) {
            if (connection.sourceNodeId() != null) {
                degrees.computeIfAbsent(connection.sourceNodeId(), id -> new Degree()).outgoing++;
            }
            if (connection.targetNodeId() != null) {
                degrees.computeIfAbsent(connection.targetNodeId(), id -> new Degree()).incoming++;
            }
        }
        return degrees;
    }

    // 노드별 입력/출력 연결 개수를 담는 내부 계산용 값 객체다.
    private static class Degree {
        private int incoming;
        private int outgoing;
    }
}
