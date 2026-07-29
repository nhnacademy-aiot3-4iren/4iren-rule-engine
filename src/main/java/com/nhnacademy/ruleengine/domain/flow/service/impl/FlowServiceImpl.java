package com.nhnacademy.ruleengine.domain.flow.service.impl;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.unauthorized.UnauthorizedFlowAccessException;
import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.connection.ConnectionRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.flow.request.FlowCreateRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.flow.request.FlowUpdateRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.flow.response.*;
import com.nhnacademy.ruleengine.domain.flow.dto.flowschedule.FlowScheduleInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.flowschedule.FlowScheduleRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeInfo;
import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeRequest;
import com.nhnacademy.ruleengine.domain.flow.entity.*;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import com.nhnacademy.ruleengine.domain.flow.repository.*;
import com.nhnacademy.ruleengine.domain.flow.service.FlowService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Validated
public class FlowServiceImpl implements FlowService {

    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;
    private final ConnectionRepository connectionRepository;
    private final FlowScheduleRepository flowScheduleRepository;
    private final FlowTemplateSensorTypeRespository flowTemplateSensorTypeRespository;

    @Transactional
    @Override
    public FlowCreateResponse createFlow(Long roomId, FlowCreateRequest request) {
        Flow flow = Flow.builder()
                .roomId(roomId)
                .flowName(request.flowName())
                .description(request.description())
                .isActive(true)
                .isTemplate(false).build();

        Flow savedFlow = flowRepository.save(flow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow, request.nodes() );
        saveConnections(savedFlow, request.connections(),tempIdMap);
//        saveSchedules(savedFlow, request.schedules());
        //TODO 이어지지 않은 노드나 순환 연결된 노드 검증 필요함1.

        return FlowCreateResponse.of(savedFlow.getId());
    }

    @Transactional
    @Override
    public FlowCreateResponse createFlowFromTemplate(Long roomId, Long templateId, FlowCreateRequest request) {
        Flow flow = Flow.builder()
                .roomId(roomId)
                .flowName(request.flowName())
                .description(request.description())
                .isActive(true)
                .isTemplate(false).build();

        Flow savedFlow = flowRepository.save(flow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow, request.nodes());
        saveConnections(savedFlow, request.connections(), tempIdMap);
//        saveSchedules(savedFlow, request.schedules());
        //TODO 이어지지 않은 노드나 순환 연결된 노드 검증 필요함2.

        return FlowCreateResponse.of(savedFlow.getId());
    }

    @Override
    public FlowListResponse getFlowList(Long roomId) {
        List<Flow> flowList = flowRepository.findAllByRoomId(roomId);

        if(flowList.isEmpty()){
            return FlowListResponse.of(List.of());
        }

        List<FlowResponse> response = flowList.stream()
                .map(f -> FlowResponse.from(
                        f, flowScheduleRepository.existsById(f.getId()))
                )
                .toList();

        return FlowListResponse.of(response);
    }

    @Override
    public FlowDetailResponse getFlowDetail(Long roomId, Long flowId) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(()-> new FlowNotFoundException(flowId));

        List<FlowSchedule> flowSchedules = flowScheduleRepository.findAllByFlowId(flowId);
        List<Node> nodes = nodeRepository.findAllByFlowId(flowId);
        List<Connection> connections = connectionRepository.findAllByFlowId(flowId);

        return FlowDetailResponse.from(flow,!flowSchedules.isEmpty(),flowSchedules, nodes,connections);
    }

    @Override
    public TemplateListResponse getFlowTemplateList(Long roomId) {
        List<Flow> templateFlow = flowRepository.findAllByIsTemplate(true);

        Map<Long,List<SensorType>> sensorTypes = getSensorTypesByFlowId(templateFlow);

        return TemplateListResponse.from(templateFlow, sensorTypes);
    }

    @Override
    public TemplateDetailResponse getTemplateFlowDetail(Long roomId, Long templateFlowId) {
        Flow tempalteFlow = flowRepository.findById(templateFlowId)
                .orElseThrow(() -> new FlowNotFoundException(templateFlowId));

        List<Node> nodes = nodeRepository.findAllByFlowId(templateFlowId);
        List<Connection> connections = connectionRepository.findAllByFlowId(templateFlowId);

        return TemplateDetailResponse.from(tempalteFlow, nodes, connections);
    }

    @Transactional
    @Override
    public void updateFlow(Long roomId, Long flowId, FlowUpdateRequest request) {
        Flow flow = flowRepository.findById(flowId).orElseThrow( () -> new FlowNotFoundException(flowId));

        flow.update(request.flowName(),request.description(), request.isActive());

        //update
        Map<Long, Long> tempIdMap = updateNodes(flow, request.nodes());
        updateConnections(flow, request.connections(),tempIdMap);
//        updateSchedules(flow, request.schedules());
        //TODO 이어지지 않은 노드나 순환 연결된 노드 검증 필요함3.
    }

    @Transactional
    @Override
    public void deleteFlow(Long roomId, Long flowId) {
        if(!flowRepository.existsByIdAndRoomId(flowId, roomId)){
            throw new UnauthorizedFlowAccessException(flowId, roomId);
        }
        flowRepository.deleteById(flowId);
    }


    private Map<Long, Long> saveNodes(Flow savedFlow, @NotEmpty List<NodeRequest> nodes) {
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
                    tempIdMap.put(n.tempNodeId(), savedNode.getId());
                });

        return tempIdMap;
    }

    private void saveConnections(Flow savedFlow, @NotNull List<ConnectionRequest> connections, Map<Long, Long> tempIdMap) {
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


    private void saveSchedules(Flow savedFlow, @NotNull List<FlowScheduleRequest> schedules) {
        if(schedules.isEmpty()){
            return;
        }

        List<FlowSchedule> scheduleList = schedules.stream()
                .map(s-> FlowSchedule.builder()
                        .flow(savedFlow)
                        .dayOfWeek(s.dayOfWeek())
                        .startTime(s.startTime())
                        .endTime(s.endTime()).build())
                .toList();

        List<FlowSchedule> savedScheduleList = flowScheduleRepository.saveAll(scheduleList);
    }


    private Map<Long, Long> updateNodes(Flow savedFlow, @NotEmpty List<NodeInfo> nodes) {
        //node
        List<Long> requestIds = nodes.stream()
                .map(NodeInfo::nodeId)
                .toList();

        //업데이트 전 노드 아이디 리스트
        List<Long> existingIds = nodeRepository.findAllByFlowId(
                savedFlow.getId()).stream()
                .map(Node::getId)
                .toList();

        //삭제 되지 않은 기존 노드 아이디 리스트(양수) TODO node_config 수정 고려해야함
        List<Long> requestExistingIds = nodes.stream()
                .filter(n -> !n.isNew())
                .map(NodeInfo :: nodeId)
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
                .filter(NodeInfo::isNew)
                .forEach(n -> {
                    Node saved = nodeRepository.save(Node.create(savedFlow, n));
                    tempIdMap.put(n.nodeId(), saved.getId());
                });

        return tempIdMap;
    }

    private void updateConnections(Flow savedFlow, @NotNull List<ConnectionInfo> connections, Map<Long, Long> tempIdMap) {

        // connection은 전체 교체
        //TODO 노드 구조가 바뀌면 어차피 재구성되는 경우가 많음 -> connection id 없이 소스노드/타겟노드 id로 복합키 PK도 괜찮을듯
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


    private void updateSchedules(Flow savedFlow, @NotNull List<FlowScheduleInfo> schedules) {
        // schedule 전체 교체
        flowScheduleRepository.deleteAllByFlowId(savedFlow.getId());

        List<FlowSchedule> newSchedules = schedules.stream()
                .map(s -> FlowSchedule.create(savedFlow, s))
                .toList();

        flowScheduleRepository.saveAll(newSchedules);
    }

    private Map<Long, List<SensorType>> getSensorTypesByFlowId(List<Flow> templateFlows){
        List<FlowTemplateSensorType> allFlowTemplateSensorTypes = flowTemplateSensorTypeRespository.findAllByFlowIn(templateFlows);
        Map<Long, List<SensorType>> sensorTypesByFlowId = allFlowTemplateSensorTypes.stream()
                .collect(Collectors.groupingBy(
                        fts -> fts.getFlow().getId(),
                        Collectors.mapping(
                                FlowTemplateSensorType::getSensorType,
                                Collectors.toList()
                        )
                ));

        return sensorTypesByFlowId;
    }
}