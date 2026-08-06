package com.nhnacademy.ruleengine.domain.templateflow.service.impl;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowTemplateSensorType;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowTemplateSensorTypeRespository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.dto.*;
import com.nhnacademy.ruleengine.domain.templateflow.service.TemplateFlowService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TemplateFlowServiceImpl implements TemplateFlowService {

    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;
    private final ConnectionRepository connectionRepository;
    private final FlowScheduleRepository flowScheduleRepository;
    private final FlowTemplateSensorTypeRespository flowTemplateSensorTypeRespository;

    @Override
    public TemplateCreateResponse createTemplatFlow(TemplateFlowCreateRequest request) {
        Flow flow = Flow.templateBuilder()
                .flowName(request.flowName()).description(request.description()).build();

        Flow savedFlow = flowRepository.save(flow);




        savedFlowTemplatemeasurementType(savedFlow, request.nodes());
        Map<Long, Long> tempIdMap = saveNodes(savedFlow, request.nodes());
        saveConnections(savedFlow, request.connections(),tempIdMap);
        validate(request.nodes(), request.connections());

        return TemplateCreateResponse.of(savedFlow.getId());
    }

    @Override
    public TemplateListResponse getTemplateList() {
        List<Flow> templateList = flowRepository.findAllByIsTemplate(true);

        if(templateList.isEmpty()){
            return TemplateListResponse.of(List.of(), List.of());
        }


        List<MeasurementType> measurementTypeList ;
//        return TemplateListResponse.of(templateList, );
        return null;
    }

    @Override
    public TemplateDetailResponse getTemplateDetail(Long templateId) {



        return null;
    }

    @Override
    public void updateTemplate(Long templateId, TemplateFlowUpdateRequest request) {

    }

    @Override
    public void deleteTemplate(Long templateId) {

    }

    private Map<Long, Long> saveNodes(Flow savedFlow, @NotEmpty List<TemplateNodeInfo> nodes) {
        Map<Long, Long> tempIdMap = new HashMap<>();

        nodes.stream()
                .forEach(n -> {
                    Node savedNode = nodeRepository.save(
                            Node.builder()
                                    .flow(savedFlow)
                                    .nodeName(n.nodeName())
                                    .nodeType(n.nodeType())
                                    .nodeConfig(n.nodeConfig())
                                    .cooldownSec(n.cooldownSec()).build()
                    );
                    tempIdMap.put(n.nodeId(), savedNode.getId());
                });

        return tempIdMap;
    }

    private void saveConnections(Flow savedFlow, @NotNull List<TemplateConnectionInfo> connections, Map<Long, Long> tempIdMap) {
        if(connections.isEmpty()){
            return;
        }
        Set<Long> savedNodeIds = tempIdMap.values().stream().collect(Collectors.toSet());


        List<Connection> connectionList = connections.stream()
                .map(c -> {
                    Long sourceId = tempIdMap.get(c.sourceNodeId());
                    Long targetId = tempIdMap.get(c.targetNodeId());

                    if (sourceId == null || targetId == null ||
                            !savedNodeIds.contains(sourceId) || !savedNodeIds.contains(targetId)) {
                        throw new InvalidConnectionException(sourceId, targetId);
                    }

                    return Connection.builder()
                            .flow(savedFlow)
                            .sourceNode(nodeRepository.getReferenceById(sourceId))
                            .targetNode(nodeRepository.getReferenceById(targetId))
                            .conditionResult(c.conditionResult()).build();
                })
                .toList();

        List<Connection> savedConnectionList = connectionRepository.saveAll(connectionList);

    }

    private void savedFlowTemplatemeasurementType(Flow savedTemplateFlow, List<TemplateNodeInfo> nodeInfoList){
        List<MeasurementType> measurementTypes = nodeInfoList.stream()
                .map(nodeInfo->nodeInfo.nodeConfig().measurementType()).toList();

        List<FlowTemplateSensorType> flowTemplateSensorTypeList = measurementTypes.stream()
                .map(m -> FlowTemplateSensorType.builder().flow(savedTemplateFlow).measurementType(m).build())
                .toList();
    }
    private Map<Long, Long> updateNodes(Flow savedFlow, @NotEmpty List<TemplateNodeInfo> nodes) {
        //node
        List<Long> requestIds = nodes.stream()
                .map(TemplateNodeInfo::nodeId)
                .toList();

        //업데이트 전 노드 아이디 리스트
        List<Long> existingIds = nodeRepository.findAllByFlowId(
                        savedFlow.getId()).stream()
                .map(Node::getId)
                .toList();

        //삭제 되지 않은 기존 노드 아이디 리스트(양수)
        List<Long> requestExistingIds = nodes.stream()
                .filter(n -> !n.isNew())
                .map(TemplateNodeInfo :: nodeId)
                .toList();

        //삭제해야할 노드
        List<Long> deleteNodesIds = existingIds.stream()
                .filter(id -> !requestExistingIds.contains(id))
                .toList();
        nodeRepository.deleteAllById(deleteNodesIds);

        // 기존 노드 수정
        Map<Long, Node> existingNodeMap = nodeRepository.findAllById(requestExistingIds)
                .stream()
                .collect(Collectors.toMap(Node::getId, n -> n));

        nodes.stream()
                .filter(n -> !n.isNew())
                .forEach(n -> {
                    Node node = existingNodeMap.get(n.nodeId());
                    if(node != null){
                        node.update(n);
                    }else{
                        throw new InvalidNodeException(n.nodeId());
                    }
                });


        // 신규 노드 저장 & 임시id → 실제id 매핑
        // key: 임시id(음수), value: 실제 저장된 id
        Map<Long, Long> tempIdMap = new HashMap<>();

        nodes.stream()
                .filter(TemplateNodeInfo::isNew)
                .forEach(n -> {
                    Node saved = nodeRepository.save(Node.create(savedFlow, n));
                    tempIdMap.put(n.nodeId(), saved.getId());
                });

        return tempIdMap;
    }

    private void updateConnections(Flow savedFlow, @NotNull List<TemplateConnectionInfo> connections, Map<Long, Long> tempIdMap) {

        // connection 전체 교체
        connectionRepository.deleteAllByFlowId(savedFlow.getId());
        connectionRepository.flush();

        List<Connection> newConnections = connections.stream()
                .map(c -> {
                    // 음수(임시id)면 실제 id로 변환
                    Long sourceId = c.sourceNodeId() < 0
                            ? tempIdMap.get(c.sourceNodeId())
                            : c.sourceNodeId();

                    Long targetId = c.targetNodeId() < 0
                            ? tempIdMap.get(c.targetNodeId())
                            : c.targetNodeId();

                    return Connection.create(savedFlow, nodeRepository.getReferenceById(sourceId),nodeRepository.getReferenceById(targetId), c.conditionResult());
                })
                .toList();

        connectionRepository.saveAll(newConnections);
    }
    //플로우 무결성 검사를 위한 메서드들
    public void validate(@NotEmpty List<TemplateNodeInfo> nodes, @NotNull List<TemplateConnectionInfo> connections){
        List<String> errors =  new ArrayList<>();

        validateNodeCount(nodes, errors);
        validateActionNodeCount(nodes, errors);
        validateNoIsolatedNode(nodes, connections, errors);
        validateSingleStartNode(nodes,connections, errors);
        validateNoCycle(nodes, connections, errors);

    }

    //노드 최소 2개 (판단 노드 1 + 행동 노드 1)
    private void validateNodeCount(@NotEmpty List<TemplateNodeInfo> nodes, List<String> errors){
        if(nodes == null || nodes.size() < 2){
            errors.add("노드는 최소 2개 이상이어야 합니다.");
        }
    }

    //행동노드 존재 여부
    private void validateActionNodeCount( List<TemplateNodeInfo> nodes, List<String> errors){
        boolean hasActionNode = nodes.stream()
                .anyMatch(n->n.nodeType().isActionNode());
        if(!hasActionNode){
            errors.add("행동 노드가 최소 1개 이상 필요합니다.");
        }
    }

    //고립 노드 존재 확인(연결이 하나도 없는 노드)
    private void validateNoIsolatedNode( List<TemplateNodeInfo> nodes, List<TemplateConnectionInfo> connections, List<String> errors){
        Set<Long> connectionNodeIds = new HashSet<>();
        connections.forEach(
                conn ->{
                    connectionNodeIds.add(conn.sourceNodeId());
                    connectionNodeIds.add(conn.targetNodeId());
                }
        );

        nodes.stream()
                .filter(node -> !connectionNodeIds.contains(node.nodeId()))
                .forEach(node -> errors.add("연결되지 않은 고립 노드가 있습니다: " + node.nodeName()));
    }

    //시작 노드 1개 만 존재 확인(incomming connection이 없는 노드)
    private void validateSingleStartNode(List<TemplateNodeInfo> nodes, List<TemplateConnectionInfo> connections, List<String> errors) {
        Set<Long> hasIncoming = connections.stream()
                .map(TemplateConnectionInfo::targetNodeId)
                .collect(Collectors.toSet());

        long startNodeCount = nodes.stream()
                .filter(node -> !hasIncoming.contains(node.nodeId()))
                .count();

        if(startNodeCount == 0){
            errors.add("시작 노드가 없습니다. 순환 연결이 의심됩니다.");
        } else if (startNodeCount > 1) {
            errors.add("시작노드는 1개여야 합니다. 현재: " + startNodeCount + "개");
        }
    }

    //순환 참조 확인(DFS)
    private void validateNoCycle( List<TemplateNodeInfo> nodes, List<TemplateConnectionInfo> connections, List<String> errors) {
        //인접 맵 구정
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
                errors.add("순환 연결이 감지되었습니다.");
                return;
            }
        }
    }

    private boolean hasCycle(Long nodeId, Map<Long, List<Long>> adjacency, Set<Long> visited, Set<Long> inStack){
        if(inStack.contains(nodeId)){
            return true; //현재 경로에서 재방문 -> 사이클 있음
        }
        if(visited.contains(nodeId)){
            return false; //이미 검사 완료된 노드
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
}
