package com.nhnacademy.ruleengine.domain.templateflow.service.impl;


import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.templateflow.repository.FlowTemplateMeasurementTypeRespository;
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
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Validated
public class TemplateFlowServiceImpl implements TemplateFlowService {

    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;
    private final ConnectionRepository connectionRepository;
    private final FlowScheduleRepository flowScheduleRepository;
    private final FlowTemplateMeasurementTypeRespository flowTemplateMeasurementTypeRespository;

    @Transactional
    @Override
    public TemplateFlowCreateResponse createTemplatFlow(TemplateFlowCreateRequest request) {
        Flow flow = Flow.templateBuilder()
                .flowName(request.flowName()).description(request.description()).build();

        Flow savedFlow = flowRepository.save(flow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow, request.nodes() );
        saveConnections(savedFlow, request.connections(),tempIdMap);
        savedFlowTemplatemeasurementType(savedFlow, request.nodes());

        validate(request.nodes(), request.connections());

        return TemplateFlowCreateResponse.of(savedFlow.getId());
    }

    @Override
    public TemplateListResponse getTemplateList() {
        List<Flow> templateList = flowRepository.findAllByIsTemplate(true);

        if(templateList.isEmpty()){
            return new TemplateListResponse(List.of());
        }
        Map<Long, List<MeasurementType>> measurementTypesByTemplateId = getMeasurementTyoesByTemplateIds(templateList);
        return TemplateListResponse.of(templateList, measurementTypesByTemplateId);
    }

    @Override
    public TemplateDetailResponse getTemplateDetail(Long templateId) {
        Flow templateFlow = flowRepository.findById(templateId).orElseThrow(FlowNotFoundException::new);



        List<Node> nodes = nodeRepository.findAllByFlowId(templateId);
        List<Connection> connections = connectionRepository.findAllByFlowId(templateId);



        return TemplateDetailResponse.from(templateFlow, nodes, connections);
    }

    @Transactional
    @Override
    public void updateTemplate(Long templateId, TemplateFlowUpdateRequest request) {
        Flow templateFlow = flowRepository.findById(templateId).orElseThrow(FlowNotFoundException::new);

        templateFlow.updateTemplate(request.flowName(), request.description());
        updateNodesNConnections(templateFlow, request.nodes(), request.connections());
        validate(request.nodes(), request.connections());
    }

    @Transactional
    @Override
    public void deleteTemplate(Long templateId) {
        if(!flowRepository.existsById(templateId)){
            throw new FlowNotFoundException();
        }
        flowRepository.deleteById(templateId);
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
                            .targetNode(nodeRepository.getReferenceById(targetId)).build();
                })
                .toList();

        List<Connection> savedConnectionList = connectionRepository.saveAll(connectionList);

    }

    private void updateNodesNConnections(Flow savedFlow, @NotEmpty List<TemplateNodeInfo> nodes, @NotNull List<TemplateConnectionInfo> connections ) {
        connectionRepository.deleteAllByFlowId(savedFlow.getId());
        nodeRepository.deleteAllByFlowId(savedFlow.getId());
        flowTemplateMeasurementTypeRespository.deleteAllByFlow(savedFlow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow,nodes);
        saveConnections(savedFlow, connections,tempIdMap);
        savedFlowTemplatemeasurementType(savedFlow, nodes);
    }


    //템플릿 플로우의 구성 센서들을 저장하는 메서드
    private void savedFlowTemplatemeasurementType(Flow savedTemplateFlow, List<TemplateNodeInfo> nodeInfoList){
        List<MeasurementType> measurementTypes = nodeInfoList.stream()
                .filter(nodeInfo -> nodeInfo.nodeConfig().nodeType().isConditionNode())
                .map(nodeInfo->nodeInfo.nodeConfig().measurementType()).toList();

        List<FlowTemplateMeasurementType> flowTemplateMeasurementTypeList = measurementTypes.stream()
                .map(m -> FlowTemplateMeasurementType.builder().flow(savedTemplateFlow).measurementType(m).build())
                .toList();

        flowTemplateMeasurementTypeRespository.saveAll(flowTemplateMeasurementTypeList);
    }

    private Map<Long, List<MeasurementType>> getMeasurementTyoesByTemplateIds(List<Flow> templateFlows){
        List<FlowTemplateMeasurementType> allFlowTemplateMeasurementTypes = flowTemplateMeasurementTypeRespository.findAllByFlowIn(templateFlows);
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

    //플로우 무결성 검사를 위한 메서드들    //TODO validator 패키지 만들어서 분리하기
    public void validate(@NotEmpty List<TemplateNodeInfo> nodes, @NotNull List<TemplateConnectionInfo> connections){
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

    //TODO 검증
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
