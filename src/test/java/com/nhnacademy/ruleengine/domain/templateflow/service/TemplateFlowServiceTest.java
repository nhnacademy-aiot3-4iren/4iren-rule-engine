package com.nhnacademy.ruleengine.domain.templateflow.service;

import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.start.StartNodeConfig;
import com.nhnacademy.ruleengine.domain.templateflow.dto.*;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.repository.FlowTemplateMeasurementTypeRepository;
import com.nhnacademy.ruleengine.domain.templateflow.validator.TemplateFlowValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateFlowServiceTest {

    @Mock private FlowRepository flowRepository;
    @Mock private NodeRepository nodeRepository;
    @Mock private ConnectionRepository connectionRepository;
    @Mock private FlowTemplateMeasurementTypeRepository flowTemplateMeasurementTypeRepository;


    private TemplateFlowValidator templateFlowValidator;

    private TemplateFlowService templateFlowService;

    @BeforeEach
    void setUp(){
        templateFlowValidator = new TemplateFlowValidator();

        templateFlowService = new TemplateFlowService(
                flowRepository,
                nodeRepository,
                connectionRepository,
                flowTemplateMeasurementTypeRepository,
                templateFlowValidator
        );
    }

    private TemplateNodeInfo createConditionNode(Long id) {
        ThresholdNodeConfig config = mock(ThresholdNodeConfig.class);
        lenient().when(config.nodeType()).thenReturn(NodeType.THRESHOLD);
        lenient().when(config.measurementType()).thenReturn(MeasurementType.TEMPERATURE);
        return new TemplateNodeInfo(id, "condition-" + id, NodeType.THRESHOLD, config);
    }

    private TemplateNodeInfo createStartNode(Long id) {
        StartNodeConfig config = mock(StartNodeConfig.class);
        lenient().when(config.nodeType()).thenReturn(NodeType.START);
        return new TemplateNodeInfo(id, "start-" + id, NodeType.START, config);
    }

    private TemplateNodeInfo createActionNode(Long id) {
        AlertNodeConfig config = mock(AlertNodeConfig.class);
        lenient().when(config.nodeType()).thenReturn(NodeType.ALERT);
        return new TemplateNodeInfo(id, "action-" + id, NodeType.ALERT, config);
    }

    private Flow createMockFlow(Long flowId, boolean isTemplate) {
        Flow flow = mock(Flow.class);
        lenient().when(flow.getId()).thenReturn(flowId);
        lenient().when(flow.getFlowName()).thenReturn("Flow " + flowId);
        lenient().when(flow.getIsTemplate()).thenReturn(isTemplate);
        return flow;
    }

    @Test
    @DisplayName("템플릿 Flow 생성 성공")
    void createTemplateFlow_success() {
        TemplateNodeInfo node1 = createStartNode(1L);
        TemplateNodeInfo node2 = createConditionNode(2L);
        TemplateNodeInfo node3 = createActionNode(3L);
        TemplateConnectionInfo conn1 = new TemplateConnectionInfo(1L, 2L, BranchType.TRUE);
        TemplateConnectionInfo conn2 = new TemplateConnectionInfo(2L, 3L, BranchType.TRUE);

        TemplateFlowCreateRequest request = new TemplateFlowCreateRequest("testTemplate", "desc", List.of(node1, node2, node3), List.of(conn1, conn2));
        Flow mockFlow = createMockFlow(100L, true);

        when(flowRepository.save(any(Flow.class))).thenReturn(mockFlow);
        Node mockNode = mock(Node.class);
        when(nodeRepository.save(any(Node.class))).thenReturn(mockNode);
        when(nodeRepository.getReferenceById(anyLong())).thenReturn(mockNode);

        TemplateFlowCreateResponse response = templateFlowService.createTemplateFlow(request);

        assertThat(response.templateFlowId()).isEqualTo(100L);
        verify(flowRepository).save(any(Flow.class));
        verify(nodeRepository, times(3)).save(any(Node.class));
        verify(connectionRepository).saveAll(anyList());
        verify(flowTemplateMeasurementTypeRepository).saveAll(anyList()); // 센서 메타데이터 저장 확인
    }

    @Test
    @DisplayName("생성 실패: 노드 개수 3개 미만 -> FlowValidationFailed")
    void createTemplateFlow_failsWhenNodeCountLessThanTwo() {
        TemplateNodeInfo node1 = createConditionNode(1L);
        TemplateFlowCreateRequest request = new TemplateFlowCreateRequest("t", "d", List.of(node1), List.of());

        assertThatThrownBy(() -> templateFlowService.createTemplateFlow(request))
                .isInstanceOf(FlowValidationFailed.class)
                .satisfies(e -> {
                    FlowValidationFailed ex = (FlowValidationFailed) e;
                    assertThat(ex.getErrors()).anyMatch(error -> error.message().contains("노드는 최소 3개"));
                });
    }

    @Test
    @DisplayName("생성 실패: 액션 노드 없음 -> FlowValidationFailed")
    void createTemplateFlow_failsWhenNoActionNode() {
        TemplateNodeInfo node1 = createStartNode(1L);
        TemplateNodeInfo node2 = createConditionNode(2L);
        TemplateNodeInfo node3 = createConditionNode(3L);
        TemplateConnectionInfo conn = new TemplateConnectionInfo(1L, 2L, BranchType.TRUE);
        TemplateConnectionInfo conn2 = new TemplateConnectionInfo(2L, 3L, BranchType.TRUE);
        TemplateFlowCreateRequest request = new TemplateFlowCreateRequest("t", "d", List.of(node1, node2, node3), List.of(conn, conn2));

        assertThatThrownBy(() -> templateFlowService.createTemplateFlow(request))
                .isInstanceOf(FlowValidationFailed.class)
                .satisfies(e -> {
                    FlowValidationFailed ex = (FlowValidationFailed) e;
                    assertThat(ex.getErrors()).anyMatch(error -> error.message().contains("행동 노드가 최소 1개"));
                });
    }

    @Test
    @DisplayName("생성 실패: 고립 노드 존재 -> FlowValidationFailed")
    void createTemplateFlow_failsWhenIsolatedNode() {
        TemplateNodeInfo node1 = createStartNode(1L);
        TemplateNodeInfo node2 = createConditionNode(2L);
        TemplateNodeInfo node3 = createActionNode(3L);
        TemplateNodeInfo node4 = createConditionNode(4L); // 고립됨
        TemplateConnectionInfo conn1 = new TemplateConnectionInfo(1L, 2L, BranchType.TRUE);
        TemplateConnectionInfo conn2 = new TemplateConnectionInfo(2L, 3L, BranchType.TRUE);
        TemplateFlowCreateRequest request = new TemplateFlowCreateRequest("t", "d", List.of(node1, node2, node3, node4), List.of(conn1, conn2));

        assertThatThrownBy(() -> templateFlowService.createTemplateFlow(request))
                .isInstanceOf(FlowValidationFailed.class)
                .satisfies(e -> {
                    FlowValidationFailed ex = (FlowValidationFailed) e;
                    assertThat(ex.getErrors()).anyMatch(error -> error.message().contains("고립 노드"));
                });
    }

    @Test
    @DisplayName("생성 실패: 순환 참조로 인한 시작 노드 없음 -> FlowValidationFailed")
    void createTemplateFlow_failsWhenCycleNoStartNode() {
        TemplateNodeInfo node1 = createStartNode(1L);
        TemplateNodeInfo node2 = createConditionNode(2L);
        TemplateNodeInfo node3 = createActionNode(3L);
        TemplateConnectionInfo conn1 = new TemplateConnectionInfo(1L, 2L, BranchType.TRUE);
        TemplateConnectionInfo conn2 = new TemplateConnectionInfo(2L, 3L, BranchType.TRUE);
        TemplateConnectionInfo conn3 = new TemplateConnectionInfo(3L, 1L, BranchType.FALSE);
        TemplateFlowCreateRequest request = new TemplateFlowCreateRequest("t", "d", List.of(node1, node2, node3), List.of(conn1, conn2, conn3));

        assertThatThrownBy(() -> templateFlowService.createTemplateFlow(request))
                .isInstanceOf(FlowValidationFailed.class)
                .satisfies(e -> {
                    FlowValidationFailed ex = (FlowValidationFailed) e;
                    assertThat(ex.getErrors()).anyMatch(error -> error.message().contains("시작 노드가 없습니다"));
                });
    }

    @Test
    @DisplayName("생성 실패: 시작 노드 여러 개 -> FlowValidationFailed")
    void createTemplateFlow_failsWhenMultipleStartNodes() {
        TemplateNodeInfo node1 = createStartNode(1L);
        TemplateNodeInfo node2 = createConditionNode(2L);
        TemplateNodeInfo node3 = createActionNode(3L);
        TemplateConnectionInfo conn1 = new TemplateConnectionInfo(1L, 3L, BranchType.TRUE);
        TemplateConnectionInfo conn2 = new TemplateConnectionInfo(2L, 3L, BranchType.TRUE);
        TemplateFlowCreateRequest request = new TemplateFlowCreateRequest("t", "d", List.of(node1, node2, node3), List.of(conn1, conn2));

        assertThatThrownBy(() -> templateFlowService.createTemplateFlow(request))
                .isInstanceOf(FlowValidationFailed.class)
                .satisfies(e -> {
                    FlowValidationFailed ex = (FlowValidationFailed) e;
                    assertThat(ex.getErrors()).anyMatch(error -> error.message().contains("시작노드는 1개여야 합니다"));
                });
    }

    @Test
    @DisplayName("생성 실패: 정의되지 않은 노드로 연결 시도 -> FlowValidationFailed")
    void createTemplateFlow_failsWithInvalidConnection() {
        TemplateNodeInfo node1 = createStartNode(1L);
        TemplateNodeInfo node2 = createConditionNode(2L);
        TemplateNodeInfo node3 = createActionNode(3L);
        TemplateConnectionInfo conn1 = new TemplateConnectionInfo(1L, 2L, BranchType.TRUE);
        TemplateConnectionInfo conn2 = new TemplateConnectionInfo(2L, 99L, BranchType.FALSE);

        TemplateFlowCreateRequest request = new TemplateFlowCreateRequest("t", "d", List.of(node1, node2, node3), List.of(conn1, conn2));
        assertThatThrownBy(() -> templateFlowService.createTemplateFlow(request))
                .isInstanceOf(FlowValidationFailed.class)
                .satisfies(e -> {
                    FlowValidationFailed ex = (FlowValidationFailed) e;
                    assertThat(ex.getErrors()).anyMatch(error -> error.message().contains("존재하지 않는 targetNodeId"));
                });
    }

    @Test
    @DisplayName("템플릿 목록 조회: 비어있는 경우")
    void getTemplateList_empty() {
        when(flowRepository.findAllByIsTemplate(true)).thenReturn(List.of());
        TemplateListResponse response = templateFlowService.getTemplateList();
        assertThat(response.templateResponseList()).isEmpty();
    }

    @Test
    @DisplayName("템플릿 목록 조회: 정상 반환 및 MeasurementType 그룹핑 확인")
    void getTemplateList_success() {
        Flow mockFlow = createMockFlow(100L, true);
        when(flowRepository.findAllByIsTemplate(true)).thenReturn(List.of(mockFlow));

        FlowTemplateMeasurementType ftmt = mock(FlowTemplateMeasurementType.class);
        when(ftmt.getFlow()).thenReturn(mockFlow);
        when(ftmt.getMeasurementType()).thenReturn(MeasurementType.TEMPERATURE);
        when(flowTemplateMeasurementTypeRepository.findAllByFlowIn(anyList())).thenReturn(List.of(ftmt));

        TemplateListResponse response = templateFlowService.getTemplateList();

        assertThat(response.templateResponseList()).hasSize(1);
        assertThat(response.templateResponseList().getFirst().measurementTypes()).containsExactly(MeasurementType.TEMPERATURE);
    }

    @Test
    @DisplayName("템플릿 상세 조회 성공")
    void getTemplateDetail_success() {
        Flow mockFlow = createMockFlow(100L, true);
        when(flowRepository.findById(100L)).thenReturn(Optional.of(mockFlow));
        when(nodeRepository.findAllByFlowId(100L)).thenReturn(List.of());
        when(connectionRepository.findAllBySourceNodeFlowId(100L)).thenReturn(List.of());

        TemplateDetailResponse response = templateFlowService.getTemplateDetail(100L);
        assertThat(response.templateId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("템플릿 상세 조회 실패: 일반 Flow 대상 -> InvalidFlowException")
    void getTemplateDetail_failsWhenNotTemplate() {
        Flow mockFlow = createMockFlow(100L, false);
        when(flowRepository.findById(100L)).thenReturn(Optional.of(mockFlow));

        assertThatThrownBy(() -> templateFlowService.getTemplateDetail(100L))
                .isInstanceOf(InvalidFlowException.class);
    }

    @Test
    @DisplayName("템플릿 수정 성공")
    void updateTemplate_success() {
        TemplateNodeInfo node1 = createStartNode(1L);
        TemplateNodeInfo node2 = createConditionNode(2L);
        TemplateNodeInfo node3 = createActionNode(3L);
        TemplateConnectionInfo conn1 = new TemplateConnectionInfo(1L, 2L, BranchType.TRUE);
        TemplateConnectionInfo conn2 = new TemplateConnectionInfo(2L, 3L, BranchType.TRUE);
        TemplateFlowUpdateRequest request = new TemplateFlowUpdateRequest("updated", "desc", List.of(node1, node2, node3), List.of(conn1, conn2));

        Flow mockFlow = createMockFlow(100L, true);
        when(flowRepository.findById(100L)).thenReturn(Optional.of(mockFlow));
        Node mockNode = mock(Node.class);
        when(nodeRepository.save(any(Node.class))).thenReturn(mockNode);
        when(nodeRepository.getReferenceById(anyLong())).thenReturn(mockNode);

        templateFlowService.updateTemplate(100L, request);

        verify(mockFlow).updateTemplate("updated", "desc");
        verify(connectionRepository).deleteAllByNodeFlowId(100L);
        verify(nodeRepository).deleteAllByFlowId(100L);
        verify(flowTemplateMeasurementTypeRepository).deleteAllByFlow(mockFlow);
        verify(connectionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("템플릿 삭제 성공")
    void deleteTemplate_success() {
        Flow mockFlow = createMockFlow(100L, true);
        when(flowRepository.findById(100L)).thenReturn(Optional.of(mockFlow));

        templateFlowService.deleteTemplate(100L);

        verify(flowRepository).deleteById(100L);
    }

    @Test
    @DisplayName("템플릿 삭제 실패: 대상이 템플릿이 아님 -> InvalidFlowException")
    void deleteTemplate_failsWhenNotTemplate() {
        Flow mockFlow = createMockFlow(100L, false);
        when(flowRepository.findById(100L)).thenReturn(Optional.of(mockFlow));

        assertThatThrownBy(() -> templateFlowService.deleteTemplate(100L))
                .isInstanceOf(InvalidFlowException.class);
    }
}
