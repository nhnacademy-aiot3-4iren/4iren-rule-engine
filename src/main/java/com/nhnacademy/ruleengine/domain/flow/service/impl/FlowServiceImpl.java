package com.nhnacademy.ruleengine.domain.flow.service.impl;

import com.nhnacademy.ruleengine.common.cache.repository.FlowCacheRepository;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.unauthorized.UnauthorizedFlowAccessException;
import com.nhnacademy.ruleengine.domain.flow.dto.*;
import com.nhnacademy.ruleengine.common.external.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.domain.flow.entity.*;
import com.nhnacademy.ruleengine.domain.flow.validator.FlowValidator;
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
    private final FlowCacheRepository flowCacheRepository;
    private final FlowValidator flowValidator;

    @Transactional
    @Override
    public FlowCreateResponse createFlow(Long roomId, FlowCreateRequest request) {
        Flow flow = Flow.regularBuilder()
                .roomId(roomId).flowName(request.flowName()).isActive(true).description(request.description()).build();

        flowValidator.validate(request.nodes(), request.connections());
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
        Map<Long,List<MeasurementType>> measurementTypesByTemplateId = getMeasurementTypesByTemplateIds(allTemplateFlowList);

        //강의실에서 측정가능한 MeasurementType List
        List<MeasurementType> measurementTypesInRoom = metaService.getMeasurementTypeOptionsInRoom(roomId);

        //강의실 MeasurementType List 기반 사용가능한 템플릿 플로우 리스트 필터링
        List<Long> availableTemplateIds = measurementTypesByTemplateId.entrySet().stream()
                .filter(entry -> new HashSet<>(measurementTypesInRoom).containsAll(entry.getValue()))
                .map(Map.Entry::getKey).toList();
        List<Flow> templateFlowList = flowRepository.findAllById(availableTemplateIds);

        return RoomTemplateListResponse.from(templateFlowList, measurementTypesByTemplateId);
    }

    @Override
    public RoomTemplateDetailResponse getTemplateFlowDetail(Long templateFlowId) {
        Flow templateFlow = flowRepository.findById(templateFlowId)
                .orElseThrow(FlowNotFoundException::new);

        if(!templateFlow.getIsTemplate()){
            throw new InvalidFlowException();
        }

        List<Node> nodes = nodeRepository.findAllByFlowId(templateFlowId);
        List<Connection> connections = connectionRepository.findAllByFlowId(templateFlowId);

        return RoomTemplateDetailResponse.from(templateFlow, nodes, connections);
    }

    @Transactional
    @Override
    public void updateFlow(Long roomId, Long flowId, FlowUpdateRequest request) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);

        flow.updateRegular(request.flowName(),request.description(), request.isActive());

        //update
        updateNodesNConnections(flow, request.nodes(), request.connections());

        flowValidator.validate(request.nodes(), request.connections());

        //캐시 무효화
        flowCacheRepository.evict(roomId);
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

        //캐시 무효화
        flowCacheRepository.evict(roomId);
    }


    //
    private Map<Long, Long> saveNodes(Flow savedFlow, @NotEmpty List<NodeInfo> nodes) {
        Map<Long, Long> tempIdMap = new HashMap<>();

        nodes.forEach(n -> {
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

    // FlowServiceImpl.java 내 saveConnections 수정
    private void saveConnections(Flow savedFlow, @NotNull List<ConnectionInfo> connections, Map<Long, Long> tempIdMap) {
        if (connections.isEmpty()) {
            return;
        }

        Set<Long> savedNodeIds = new HashSet<>(tempIdMap.values());
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
                            .branchType(String.valueOf(c.branchType()))
                            .build();
                })
                .toList();
        connectionRepository.saveAll(connectionList);
    }

    private void updateNodesNConnections(Flow savedFlow, @NotEmpty List<NodeInfo> nodes, @NotNull List<ConnectionInfo> connections ) {
        connectionRepository.deleteAllByFlowId(savedFlow.getId());
        nodeRepository.deleteAllByFlowId(savedFlow.getId());
        Map<Long, Long> tempIdMap = saveNodes(savedFlow,nodes);
        saveConnections(savedFlow, connections,tempIdMap);

    }

    private Map<Long, List<MeasurementType>> getMeasurementTypesByTemplateIds(List<Flow> templateFlows){
        List<FlowTemplateMeasurementType> allFlowTemplateMeasurementTypes = flowTemplateMeasurementTypeRepository.findAllByFlowIn(templateFlows);
        return allFlowTemplateMeasurementTypes.stream()
                .collect(Collectors.groupingBy(
                        fts -> fts.getFlow().getId(),
                        Collectors.mapping(
                                FlowTemplateMeasurementType::getMeasurementType,
                                Collectors.toList()
                        )
                ));
    }
}