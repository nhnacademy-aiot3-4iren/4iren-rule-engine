package com.nhnacademy.ruleengine.domain.flow.service;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.unauthorized.UnauthorizedFlowAccessException;
import com.nhnacademy.ruleengine.domain.flow.dto.*;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.flow.validator.FlowValidator;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.repository.FlowTemplateMeasurementTypeRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Validated
public class FlowService {

    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;
    private final ConnectionRepository connectionRepository;
    private final FlowTemplateMeasurementTypeRepository flowTemplateMeasurementTypeRepository;
    private final FlowScheduleRepository flowScheduleRepository;

    private final RoomSensorMetaService metaService;
//    private final FlowCacheRepository flowCacheRepository;
    private final FlowValidator flowValidator;

    @Transactional
    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public FlowCreateResponse createFlow(Long roomId, FlowCreateRequest request) {
        Flow flow = Flow.regularBuilder()
                .roomId(roomId).flowName(request.flowName()).isActive(request.isActive()).description(request.description()).build();

        flowValidator.validate(request.nodes(), request.connections(), metaService.getSensorMetaList(roomId));
        Flow savedFlow = flowRepository.save(flow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow, request.nodes() );
        saveConnections(savedFlow, request.connections(),tempIdMap);

        return FlowCreateResponse.of(savedFlow.getId());
    }

    public FlowListResponse getFlowList(Long roomId) {
        List<Flow> flowList = flowRepository.findAllByRoomId(roomId);

        if(flowList.isEmpty()){
            return FlowListResponse.of(List.of());
        }

        List<FlowSchedule> flowScheduleList = flowScheduleRepository.findAllByFlowIdIn(flowList.stream().map(Flow::getId).toList());

        Map<Long, Long> scheduleCountMap = flowScheduleList.stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getFlow().getId(),
                        Collectors.counting()
                ));
        List<FlowResponse> response = FlowResponse.fromList(flowList,scheduleCountMap);
        return FlowListResponse.of(response);
    }

    public FlowDetailResponse getFlowDetail(Long roomId, Long flowId) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);

        if(flow.getIsTemplate()){
            throw new InvalidFlowException();
        }
        List<Node> nodes = nodeRepository.findAllByFlowId(flowId);
        List<Connection> connections = connectionRepository.findAllBySourceNodeFlowId(flowId);
        List<SensorMetaInfo> sensorMetaInfoList = metaService.getSensorMetaList(roomId);


        return FlowDetailResponse.from(flow,nodes,connections, sensorMetaInfoList);
    }

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

    public RoomTemplateDetailResponse getTemplateFlowDetail(Long roomId, Long templateFlowId) {
        Flow templateFlow = flowRepository.findById(templateFlowId)
                .orElseThrow(FlowNotFoundException::new);

        if(!templateFlow.getIsTemplate()){
            throw new InvalidFlowException();
        }

        List<Node> nodes = nodeRepository.findAllByFlowId(templateFlowId);
        List<Connection> connections = connectionRepository.findAllBySourceNodeFlowId(templateFlowId);
        List<SensorMetaInfo> sensorMetaInfoList = metaService.getSensorMetaList(roomId);


        return RoomTemplateDetailResponse.from(templateFlow, nodes, connections, sensorMetaInfoList);
    }


    @Transactional
    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public void updateFlow(Long roomId, Long flowId, FlowUpdateRequest request) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);
        flowValidator.validate(request.nodes(), request.connections(), metaService.getSensorMetaList(roomId));

        flow.updateRegular(request.flowName(),request.description(), request.isActive());

        //update
        updateNodesNConnections(flow, request.nodes(), request.connections());
    }

    @Transactional
    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public void deleteFlow(Long roomId, Long flowId) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId)
                .orElseThrow(UnauthorizedFlowAccessException::new);
        if (flow.getIsTemplate()) {
            throw new InvalidFlowException();
        }
        flowRepository.deleteById(flowId);


    }

    @Transactional
    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public void updateStatus(Long roomId, Long flowId, UpdateFlowStatusRequest request) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId)
                .orElseThrow(UnauthorizedFlowAccessException::new);

        flow.updateStatus(request.isActive());
    }

    public FlowBuildFormResponse getFlowBuildForm(Long roomId) {

        List<SensorMetaInfo> sensorMetaInfoList = metaService.getSensorMetaList(roomId);

        return FlowBuildFormResponse.of(roomId, sensorMetaInfoList);
    }


    //
    private Map<Long, Long> saveNodes(Flow savedFlow, @NotEmpty List<NodeInfo> nodes) {
        Map<Long, Long> tempIdMap = new HashMap<>();

        nodes.forEach(n -> {
                    Node savedNode = nodeRepository.save(Node.create(savedFlow, n));
                    tempIdMap.put(n.nodeId(), savedNode.getId());
                });

        return tempIdMap;
    }

    // FlowService.java 내 saveConnections 수정
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
                            .sourceNode(nodeRepository.getReferenceById(sourceId))
                            .targetNode(nodeRepository.getReferenceById(targetId))
                            .branchType(String.valueOf(c.branchType()))
                            .build();
                })
                .toList();
        connectionRepository.saveAll(connectionList);
    }

    private void updateNodesNConnections(Flow savedFlow, @NotEmpty List<NodeInfo> nodes, @NotNull List<ConnectionInfo> connections ) {
        connectionRepository.deleteAllByNodeFlowId(savedFlow.getId());
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
