package com.nhnacademy.ruleengine.domain.templateflow.service;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.dto.*;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.repository.FlowTemplateMeasurementTypeRepository;
import com.nhnacademy.ruleengine.domain.templateflow.validator.TemplateFlowValidator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Validated
@Slf4j
public class TemplateFlowService {

    private final FlowRepository flowRepository;
    private final NodeRepository nodeRepository;
    private final ConnectionRepository connectionRepository;
    private final FlowTemplateMeasurementTypeRepository flowTemplateMeasurementTypeRepository;

    private final TemplateFlowValidator templateFlowValidator;

    @Transactional
    public TemplateFlowCreateResponse createTemplateFlow(TemplateFlowCreateRequest request) {
        log.info("템플릿 플로우 생성 처리 시작 templateName={}, nodeCount={}, connectionCount={}",
                request.flowName(), request.nodes().size(), request.connections().size());
        Flow flow = Flow.templateBuilder()
                .flowName(request.flowName()).description(request.description()).build();

        templateFlowValidator.validate(request.nodes(), request.connections());
        Flow savedFlow = flowRepository.save(flow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow, request.nodes() );
        saveConnections(savedFlow, request.connections(),tempIdMap);
        savedFlowTemplateMeasurementType(savedFlow, request.nodes());


        log.info("템플릿 플로우 생성 완료 templateId={}, nodeCount={}, connectionCount={}",
                savedFlow.getId(), request.nodes().size(), request.connections().size());
        return TemplateFlowCreateResponse.of(savedFlow.getId());
    }

    public TemplateListResponse getTemplateList() {
        List<Flow> templateList = flowRepository.findAllByIsTemplate(true);

        if(templateList.isEmpty()){
            log.info("템플릿 플로우 목록 조회 완료 templateCount=0");
            return new TemplateListResponse(List.of());
        }
        Map<Long, List<MeasurementType>> measurementTypesByTemplateId = getMeasurementTypesByTemplateIds(templateList);
        log.info("템플릿 플로우 목록 조회 완료 templateCount={}", templateList.size());
        return TemplateListResponse.of(templateList, measurementTypesByTemplateId);
    }

    public TemplateDetailResponse getTemplateDetail(Long templateId) {
        Flow templateFlow = flowRepository.findById(templateId).orElseThrow(FlowNotFoundException::new);

        if (!templateFlow.getIsTemplate()) {
            throw new InvalidFlowException();
        }

        List<Node> nodes = nodeRepository.findAllByFlowId(templateId);
        List<Connection> connections = connectionRepository.findAllBySourceNodeFlowId(templateId);



        log.info("템플릿 플로우 상세 조회 완료 templateId={}, nodeCount={}, connectionCount={}",
                templateId, nodes.size(), connections.size());
        return TemplateDetailResponse.from(templateFlow, nodes, connections);
    }

    @Transactional
    public void updateTemplate(Long templateId, TemplateFlowUpdateRequest request) {
        log.info("템플릿 플로우 수정 처리 시작 templateId={}, nodeCount={}, connectionCount={}",
                templateId, request.nodes().size(), request.connections().size());
        Flow templateFlow = flowRepository.findById(templateId).orElseThrow(FlowNotFoundException::new);
        templateFlowValidator.validate(request.nodes(), request.connections());

        if (!templateFlow.getIsTemplate()) {
            throw new InvalidFlowException();
        }

        templateFlow.updateTemplate(request.flowName(), request.description());
        updateNodesNConnections(templateFlow, request.nodes(), request.connections());
        log.info("템플릿 플로우 수정 완료 templateId={}", templateId);
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        Flow templateFlow = flowRepository.findById(templateId).orElseThrow(FlowNotFoundException::new);

        if(!templateFlow.getIsTemplate()){
            throw new InvalidFlowException();
        }
        flowRepository.deleteById(templateId);
        log.info("템플릿 플로우 삭제 완료 templateId={}", templateId);
    }

    private Map<Long, Long> saveNodes(Flow savedFlow, @NotEmpty List<TemplateNodeInfo> nodes) {
        Map<Long, Long> tempIdMap = new HashMap<>();

        nodes.forEach(n -> {
            Node savedNode = nodeRepository.save(
                    Node.builder()
                            .flow(savedFlow)
                            .nodeName(n.nodeName())
                            .nodeType(n.nodeType())
                            .nodeConfig(n.nodeConfig())
                            .build()
                    );
                    tempIdMap.put(n.nodeId(), savedNode.getId());
                });

        log.info("템플릿 플로우 노드 저장 완료 templateId={}, nodeCount={}", savedFlow.getId(), tempIdMap.size());
        return tempIdMap;
    }

    private void saveConnections(Flow savedFlow, @NotNull List<TemplateConnectionInfo> connections, Map<Long, Long> tempIdMap) {
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
        log.info("템플릿 플로우 연결 저장 완료 templateId={}, connectionCount={}", savedFlow.getId(), connectionList.size());
    }

    private void updateNodesNConnections(Flow savedFlow, @NotEmpty List<TemplateNodeInfo> nodes, @NotNull List<TemplateConnectionInfo> connections ) {
        log.info("템플릿 플로우 노드와 연결 재구성 시작 templateId={}", savedFlow.getId());
        connectionRepository.deleteAllByNodeFlowId(savedFlow.getId());
        nodeRepository.deleteAllByFlowId(savedFlow.getId());
        flowTemplateMeasurementTypeRepository.deleteAllByFlow(savedFlow);

        Map<Long, Long> tempIdMap = saveNodes(savedFlow,nodes);
        saveConnections(savedFlow, connections,tempIdMap);
        savedFlowTemplateMeasurementType(savedFlow, nodes);
        log.info("템플릿 플로우 노드와 연결 재구성 완료 templateId={}", savedFlow.getId());
    }


    //템플릿 플로우의 구성 센서들을 저장하는 메서드
    private void savedFlowTemplateMeasurementType(Flow savedTemplateFlow, List<TemplateNodeInfo> nodeInfoList){
        List<MeasurementType> measurementTypes = nodeInfoList.stream()
                .filter(nodeInfo -> nodeInfo.nodeConfig().nodeType().isConditionNode())
                .map(nodeInfo->nodeInfo.nodeConfig().measurementType()).toList();

        List<FlowTemplateMeasurementType> flowTemplateMeasurementTypeList = measurementTypes.stream()
                .distinct()
                .map(m -> FlowTemplateMeasurementType.builder().flow(savedTemplateFlow).measurementType(m).build())
                .toList();

        flowTemplateMeasurementTypeRepository.saveAll(flowTemplateMeasurementTypeList);
        log.info("템플릿 플로우 측정 항목 저장 완료 templateId={}, measurementTypeCount={}",
                savedTemplateFlow.getId(), flowTemplateMeasurementTypeList.size());
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
