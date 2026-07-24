package com.nhnacademy.ruleengine.domain.flow.service.impl;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
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
                .isActive(request.isActive())
                .isTemplate(false).build();

        Flow savedFlow = flowRepository.save(flow);

        saveNodes(savedFlow, request.nodes() );
        saveSchedules(savedFlow, request.schedules());
        saveConnections(savedFlow, request.connections());
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
                .isActive(request.isActive())
                .isTemplate(false).build();

        Flow savedFlow = flowRepository.save(flow);

        saveNodes(savedFlow, request.nodes());
        saveSchedules(savedFlow, request.schedules());
        saveConnections(savedFlow, request.connections());

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

//        nodeRepository.deleteAllByFlowId(flowId);
//        connectionRepository.deleteAllByFlowId(flowId);
//        flowScheduleRepository.deleteAllByFlowId(flowId);

        updateNodes(flow, request.nodes());
        updateConnections(flow, request.connections());
        updateSchedules(flow, request.schedules());


        //TODO 이어지지 않은 노드나 순환 연결된 노드 검증 필요함3.

        //TODO 변경 내용만 바꿀수 있도록 바꾸기
//
//        //node
//        //1, 3 -> 2, 3
//        //업데이트 전 노드 리스트
//        List<Node> nodes = nodeRepository.findAllByFlowId(flowId);
//
//        List<Long> ids = nodes.stream()
//                .map(n -> n.getId())
//                .toList();
//
//
//        //업데이트 후 노드 리스트
//        List<Long> updateNodeIds = request.nodes().stream()
//                .map(n ->n.nodeId())
//                .toList();
//
//        //삭제해야할 노드 리스트
//        List<Long> deleteNodesIds = ids.stream()
//                .filter(id -> !updateNodeIds.contains(id))
//                .toList();
//
//        //추가해야 할 노드 리스트
//        List<Long> addNodeIds = updateNodeIds.stream()
//                .filter(u -> !ids.contains(u))
//                .toList();
//
//        nodeRepository.saveAll()
//
//        //connection
//        List<Connection>  connections;
    }

    @Transactional
    @Override
    public void deleteFlow(Long roomId, Long flowId) {
        if(!flowRepository.existsByIdAndRoomId(flowId, roomId)){
            throw new UnauthorizedFlowAccessException(flowId, roomId);
        }
        flowRepository.deleteById(flowId);
    }

    private void saveNodes(Flow savedFlow, @NotEmpty List<NodeRequest> nodes) {

        List<Node> nodeList = nodes.stream()
                .map(n -> Node.builder()
                        .flow(savedFlow)
                        .nodeName(n.nodeName())
                        .nodeType(n.nodeType())
                        .nodeConfig(n.nodeConfig())
                        .cooldownSec(n.cooldownSec()).build())
                .toList();

        List<Node> savedNodeList = nodeRepository.saveAll(nodeList);

    }

    private void saveConnections(Flow savedFlow, @NotNull List<ConnectionRequest> connections) {
        if(connections.isEmpty()){
            return;
        }
        Set<Long> nodeIdsInFlow = nodeRepository.findAllByFlowId(savedFlow.getId()).stream()
                .map(Node :: getId)
                .collect(Collectors.toSet());

        List<Connection> connectionList = connections.stream()
                .map(c -> {
                    if(!nodeRepository.existsById(c.sourceNodeId()) || !nodeRepository.existsById(c.targetNodeId())) {
                        throw new InvalidConnectionException(c.sourceNodeId(), c.targetNodeId());
                    }
                    if(nodeIdsInFlow.contains(c.sourceNodeId())){
                        throw new InvalidNodeException(c.sourceNodeId());
                    }if(nodeIdsInFlow.contains(c.targetNodeId())){
                        throw new InvalidNodeException(c.targetNodeId());

                    }

                        return Connection.builder()
                                .flow(savedFlow)
                                .sourceNode(nodeRepository.getReferenceById(c.sourceNodeId()))
                                .targetNode(nodeRepository.getReferenceById(c.targetNodeId()))
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


//TODO 추가 구현 해야함

    private void updateNodes(Flow savedFlow, @NotEmpty List<NodeInfo> nodes) {
    }

    private void updateConnections(Flow savedFlow, @NotNull List<ConnectionInfo> connections) {
    }


    private void updateSchedules(Flow savedFlow, @NotNull List<FlowScheduleInfo> schedules) {
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