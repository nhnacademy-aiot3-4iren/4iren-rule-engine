package com.nhnacademy.ruleengine.domain.flow.service.impl;

import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.unauthorized.UnauthorizedFlowAccessException;
import com.nhnacademy.ruleengine.domain.flow.dto.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.domain.flow.entity.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.flow.repository.*;
import com.nhnacademy.ruleengine.domain.flow.service.FlowService;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.repository.FlowTemplateMeasurementTypeRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Validated
public class FlowServiceImpl implements FlowService {

    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;
    private final ConnectionRepository connectionRepository;
    private final FlowTemplateMeasurementTypeRepository flowTemplateMeasurementTypeRepository;

    private final SensorStaticMetaService metaService;

    @Transactional
    @Override
    public FlowCreateResponse createFlow(Long roomId, FlowCreateRequest request) {
        Flow flow = Flow.regularBuilder()
                .roomId(roomId).flowName(request.flowName()).description(request.description()).build();

        validate(request.nodes(), request.connections());
        Flow savedFlow = flowRepository.save(flow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow, request.nodes() );
        saveConnections(savedFlow, request.connections(),tempIdMap);

        return FlowCreateResponse.of(savedFlow.getId());
    }


    @Override
    public FlowListResponse getFlowList(Long roomId) {
        List<Flow> flowList = flowRepository.findAllByRoomId(roomId);

        if(flowList.isEmpty()){
            return FlowListResponse.of(List.of());
        }

        List<FlowResponse> response = FlowResponse.fromList(flowList);
        return FlowListResponse.of(response);
    }

    @Override
    public FlowDetailResponse getFlowDetail(Long roomId, Long flowId) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);

        if(flow.getIsTemplate()){
            throw new InvalidFlowException();
        }
        List<Node> nodes = nodeRepository.findAllByFlowId(flowId);
        List<Connection> connections = connectionRepository.findAllByFlowId(flowId);

        return FlowDetailResponse.from(flow,nodes,connections);
    }

    @Override
    public RoomTemplateListResponse getFlowTemplateList(Long roomId) {
        List<Flow> allTemplateFlowList = flowRepository.findAllByIsTemplate(true);

        //템플릿 플로우 id별 필요한 MeasurementType List
        Map<Long,List<MeasurementType>> measurementTypesByTemplateId = getMeasurementTyoesByTemplateIds(allTemplateFlowList);

        //강의실에서 측정가능한 MeasurementType List
        List<MeasurementType> measurementTypesInRoom = metaService.getMeasurementTypeOptionsInRoom(roomId);

        //강의실 MeasurementType List 기반 사용가능한 템플릿 플로우 리스트 필터링
        List<Long> availableTemplateIds = measurementTypesByTemplateId.entrySet().stream()
                .filter(entry -> measurementTypesInRoom.containsAll(entry.getValue()))
                .map(Map.Entry::getKey).toList();
        List<Flow> templateFlowList = flowRepository.findAllById(availableTemplateIds);

        return RoomTemplateListResponse.from(templateFlowList, measurementTypesByTemplateId);
    }

    @Override
    public RoomTemplateDetailResponse getTemplateFlowDetail(Long templateFlowId) {
        Flow tempalteFlow = flowRepository.findById(templateFlowId)
                .orElseThrow(FlowNotFoundException::new);

        if(!tempalteFlow.getIsTemplate()){
            throw new InvalidFlowException();
        }

        List<Node> nodes = nodeRepository.findAllByFlowId(templateFlowId);
        List<Connection> connections = connectionRepository.findAllByFlowId(templateFlowId);

        return RoomTemplateDetailResponse.from(tempalteFlow, nodes, connections);
    }

    @Transactional
    @Override
    public void updateFlow(Long roomId, Long flowId, FlowUpdateRequest request) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);

        flow.updateRegular(request.flowName(),request.description(), request.isActive());

        //update
        updateNodesNConnections(flow, request.nodes(), request.connections());

        validate(request.nodes(), request.connections());
    }

    @Transactional
    @Override
    public void deleteFlow(Long roomId, Long flowId) {
        if(!flowRepository.existsByIdAndRoomId(flowId, roomId)){
            throw new UnauthorizedFlowAccessException();
        }
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId)
                .orElseThrow(UnauthorizedFlowAccessException::new);
        if (flow.getIsTemplate()) {
            throw new InvalidFlowException();
        }
        flowRepository.deleteById(flowId);
    }


    //
    private Map<Long, Long> saveNodes(Flow savedFlow, @NotEmpty List<NodeInfo> nodes) {
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

    private void saveConnections(Flow savedFlow, @NotNull List<ConnectionInfo> connections, Map<Long, Long> tempIdMap) {
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
                            .targetNode(nodeRepository.getReferenceById(targetId)).build();
                })
                .toList();

        List<Connection> savedConnectionList = connectionRepository.saveAll(connectionList);

    }

    private void updateNodesNConnections(Flow savedFlow, @NotEmpty List<NodeInfo> nodes, @NotNull List<ConnectionInfo> connections ) {
        connectionRepository.deleteAllByFlowId(savedFlow.getId());
        nodeRepository.deleteAllByFlowId(savedFlow.getId());
        Map<Long, Long> tempIdMap = saveNodes(savedFlow,nodes);
        saveConnections(savedFlow, connections,tempIdMap);

    }

    private Map<Long, List<MeasurementType>> getMeasurementTyoesByTemplateIds(List<Flow> templateFlows){
        List<FlowTemplateMeasurementType> allFlowTemplateMeasurementTypes = flowTemplateMeasurementTypeRepository.findAllByFlowIn(templateFlows);
        Map<Long, List<MeasurementType>> measurementTypesByTemplateId = allFlowTemplateMeasurementTypes.stream()
                .collect(Collectors.groupingBy(
                        fts -> fts.getFlow().getId(),
                        Collectors.mapping(
                                FlowTemplateMeasurementType::getMeasurementType,
                                Collectors.toList()
                        )
                ));
        return measurementTypesByTemplateId;
    }

    //TODO validator 패키지 만들어서 분리하기
    //플로우 무결성 검사를 위한 메서드들
    public void validate(@NotEmpty List<NodeInfo> nodes, @NotNull List<ConnectionInfo> connections){
        List<String> errors =  new ArrayList<>();

        validateNodeCount(nodes, errors);
        validateActionNodeCount(nodes, errors);
        validateNoIsolatedNode(nodes, connections, errors);
        validateSingleStartNode(nodes,connections, errors);
        validateNoCycle(nodes, connections, errors);

        if(!errors.isEmpty()){
            throw new FlowValidationFailed(errors);
        }
    }

    //노드 최소 2개 (판단 노드 1 + 행동 노드 1)
    private void validateNodeCount(@NotEmpty List<NodeInfo> nodes, List<String> errors){
        if(nodes == null || nodes.size() < 2){
            errors.add("노드는 최소 2개 이상이어야 합니다.");
        }
    }

    //행동노드 존재 여부
    private void validateActionNodeCount( List<NodeInfo> nodes, List<String> errors){
        boolean hasActionNode = nodes.stream()
                .anyMatch(n->n.nodeType().isActionNode());
        if(!hasActionNode){
            errors.add("행동 노드가 최소 1개 이상 필요합니다.");
        }
    }

    //고립 노드 존재 확인(연결이 하나도 없는 노드)
    private void validateNoIsolatedNode( List<NodeInfo> nodes, List<ConnectionInfo> connections, List<String> errors){
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
    private void validateSingleStartNode(List<NodeInfo> nodes, List<ConnectionInfo> connections, List<String> errors) {
        Set<Long> hasIncoming = connections.stream()
                .map(ConnectionInfo::targetNodeId)
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

    //순환 참조 확인(DFS) TODO 검증
    private void validateNoCycle( List<NodeInfo> nodes, List<ConnectionInfo> connections, List<String> errors) {
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