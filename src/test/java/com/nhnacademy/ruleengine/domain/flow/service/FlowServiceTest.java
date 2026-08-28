package com.nhnacademy.ruleengine.domain.flow.service;

import com.nhnacademy.ruleengine.common.cache.repository.FlowCacheRepository;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.unauthorized.UnauthorizedFlowAccessException;
import com.nhnacademy.ruleengine.common.external.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.domain.flow.dto.*;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.flow.validator.FlowValidator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.repository.FlowTemplateMeasurementTypeRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowServiceTest {

    @Mock private FlowRepository flowRepository;
    @Mock private NodeRepository nodeRepository;
    @Mock private ConnectionRepository connectionRepository;
    @Mock private FlowTemplateMeasurementTypeRepository flowTemplateMeasurementTypeRepository;
    @Mock private SensorStaticMetaService metaService;
    @Mock private FlowCacheRepository flowCacheRepository;

    private FlowValidator flowValidator;

    private FlowService flowService;

    @BeforeEach
    void setUp(){
        flowValidator = new FlowValidator();

        flowService = new FlowService(
                flowRepository,
                nodeRepository,
                connectionRepository,
                flowTemplateMeasurementTypeRepository,
                metaService,
                flowCacheRepository,
                flowValidator
        );
    }

    private NodeInfo createConditionNode(Long id) {
        return new NodeInfo(id, "condition", NodeType.THRESHOLD, mock(ThresholdNodeConfig.class), 0);
    }
    private NodeInfo createActionNode(Long id) {
        return new NodeInfo(id, "action", NodeType.ALERT, mock(AlertNodeConfig.class), 0);
    }
    private Flow createMockFlow(Long flowId, boolean isTemplate) {
        Flow flow = mock(Flow.class);
        lenient().when(flow.getId()).thenReturn(flowId);
        lenient().when(flow.getFlowName()).thenReturn("Flow " + flowId);
        lenient().when(flow.getIsTemplate()).thenReturn(isTemplate);
        return flow;
    }

    @Test
    @DisplayName("Flow 생성 성공")
    void createFlow_success() {
        NodeInfo node1 = createConditionNode(1L);
        NodeInfo node2 = createActionNode(2L);
        ConnectionInfo conn = new ConnectionInfo(1L, 2L, BranchType.TRUE);

        FlowCreateRequest request = new FlowCreateRequest("testFlow", "desc",true, List.of(node1, node2), List.of(conn));
        Flow mockFlow = mock(Flow.class);
        when(mockFlow.getId()).thenReturn(100L);
        when(flowRepository.save(any(Flow.class))).thenReturn(mockFlow);

        Node mockNode = mock(Node.class);
        when(nodeRepository.save(any(Node.class))).thenReturn(mockNode);
        when(nodeRepository.getReferenceById(anyLong())).thenReturn(mockNode);

        FlowCreateResponse response = flowService.createFlow(1L, request);

        assertThat(response.flowId()).isEqualTo(100L);
        verify(flowRepository).save(any(Flow.class));
        verify(nodeRepository, times(2)).save(any(Node.class));
        verify(connectionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("노드 개수 2개 미만 -> FlowValidationFailed")
    void createFlow_nodeCount() {
        NodeInfo node1 = createConditionNode(1L);
        FlowCreateRequest request = new FlowCreateRequest("flow", "desc", true, List.of(node1), List.of());

        assertThatThrownBy(() -> flowService.createFlow(1L, request)).isInstanceOf(FlowValidationFailed.class);
    }

    @Test
    @DisplayName("액션 노드가 하나도 없음 -> FlowValidationFailed")
    void createFlow_noActionNode() {
        NodeInfo node1 = createConditionNode(1L);
        NodeInfo node2 = createConditionNode(2L);
        ConnectionInfo conn = new ConnectionInfo(1L, 2L, BranchType.TRUE);
        FlowCreateRequest request = new FlowCreateRequest("flow", "desc", true, List.of(node1, node2), List.of(conn));

        assertThatThrownBy(() -> flowService.createFlow(1L, request)).isInstanceOf(FlowValidationFailed.class);
    }

    @Test
    @DisplayName("연결되지 않은 노드 있음 -> FlowValidationFailed")
    void createFlow_isolatedNode() {
        NodeInfo node1 = createConditionNode(1L);
        NodeInfo node2 = createActionNode(2L);
        NodeInfo node3 = createConditionNode(3L);
        ConnectionInfo conn = new ConnectionInfo(1L, 2L, BranchType.TRUE);
        FlowCreateRequest request = new FlowCreateRequest("flow", "desc", true, List.of(node1, node2, node3), List.of(conn));

        assertThatThrownBy(() -> flowService.createFlow(1L, request)).isInstanceOf(FlowValidationFailed.class);
    }

    @Test
    @DisplayName("순환 구조 -> FlowValidationFailed")
    void createFlow_cycle() {
        NodeInfo node1 = createConditionNode(1L);
        NodeInfo node2 = createActionNode(2L);
        ConnectionInfo conn1 = new ConnectionInfo(1L, 2L, BranchType.TRUE);
        ConnectionInfo conn2 = new ConnectionInfo(2L, 1L, BranchType.FALSE);
        FlowCreateRequest request = new FlowCreateRequest("flow", "desc", true, List.of(node1, node2), List.of(conn1, conn2));

        assertThatThrownBy(() -> flowService.createFlow(1L, request)).isInstanceOf(FlowValidationFailed.class);
    }

    @Test
    @DisplayName("존재하지 않는 노드를 연결하려고 함 -> InvalidConnectionException")
    void createFlow_failsWithInvalidConnection() {
        NodeInfo node1 = createConditionNode(1L);
        NodeInfo node2 = createActionNode(2L);

        ConnectionInfo validConn = new ConnectionInfo(1L, 2L, BranchType.TRUE);
        ConnectionInfo invalidConn = new ConnectionInfo(2L, 3L, BranchType.TRUE);

        FlowCreateRequest request = new FlowCreateRequest(
                "testFlow", "desc", true, List.of(node1, node2), List.of(validConn, invalidConn)
        );
        Flow mockFlow = createMockFlow(100L, false);

        when(flowRepository.save(any(Flow.class))).thenReturn(mockFlow);
        when(nodeRepository.save(any(Node.class))).thenReturn(mock(Node.class));

        assertThatThrownBy(() -> flowService.createFlow(1L, request)).isInstanceOf(InvalidConnectionException.class);
    }

    @Test
    @DisplayName("플로우 목록 조회")
    void getFlowList_success() {
        Flow mockFlow = createMockFlow(100L, false);
        when(flowRepository.findAllByRoomId(1L)).thenReturn(List.of(mockFlow));

        FlowListResponse response = flowService.getFlowList(1L);

        assertThat(response.flowResponseList()).hasSize(1);
        assertThat(response.flowResponseList().getFirst().flowId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("플로우 목록 조회: 비어있는 경우")
    void getFlowList_empty() {
        when(flowRepository.findAllByRoomId(1L)).thenReturn(List.of());

        FlowListResponse response = flowService.getFlowList(1L);

        assertThat(response.flowResponseList()).isEmpty();
    }

    @Test
    @DisplayName("플로우 상세 조회")
    void getFlowDetail_success() {
        Flow mockFlow = createMockFlow(100L, false);
        when(flowRepository.findByIdAndRoomId(100L, 1L)).thenReturn(Optional.of(mockFlow));
        when(nodeRepository.findAllByFlowId(100L)).thenReturn(List.of());
        when(connectionRepository.findAllByFlowId(100L)).thenReturn(List.of());

        FlowDetailResponse response = flowService.getFlowDetail(1L, 100L);

        assertThat(response.flowId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("존재하지 않는 플로우 -> FlowNotFoundException")
    void getFlowDetail_notFound() {
        when(flowRepository.findByIdAndRoomId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flowService.getFlowDetail(1L, 100L)).isInstanceOf(FlowNotFoundException.class);
    }

    @Test
    @DisplayName("대상이 템플릿임 -> InvalidFlowException")
    void getFlowDetail_failsWhenIsTemplate() {
        Flow mockFlow = createMockFlow(100L, true);
        when(flowRepository.findByIdAndRoomId(100L, 1L)).thenReturn(Optional.of(mockFlow));

        assertThatThrownBy(() -> flowService.getFlowDetail(1L, 100L)).isInstanceOf(InvalidFlowException.class);
    }

    @Test
    @DisplayName("템플릿 목록 조회")
    void getFlowTemplateList_success() {
        Flow templateFlow = createMockFlow(200L, true);
        when(flowRepository.findAllByIsTemplate(true)).thenReturn(List.of(templateFlow));

        FlowTemplateMeasurementType ftmt = mock(FlowTemplateMeasurementType.class);
        when(ftmt.getFlow()).thenReturn(templateFlow);
        when(ftmt.getMeasurementType()).thenReturn(MeasurementType.TEMPERATURE);

        when(flowTemplateMeasurementTypeRepository.findAllByFlowIn(anyList())).thenReturn(List.of(ftmt));
        when(metaService.getMeasurementTypeOptionsInRoom(1L)).thenReturn(List.of(MeasurementType.TEMPERATURE, MeasurementType.CO2));
        when(flowRepository.findAllById(List.of(200L))).thenReturn(List.of(templateFlow));

        RoomTemplateListResponse response = flowService.getFlowTemplateList(1L);

        assertThat(response.roomTemplateResponseList()).hasSize(1);
        assertThat(response.roomTemplateResponseList().getFirst().templateId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("템플릿 상세 조회")
    void getTemplateFlowDetail_success() {
        Flow mockFlow = createMockFlow(200L, true);
        when(flowRepository.findById(200L)).thenReturn(Optional.of(mockFlow));
        when(nodeRepository.findAllByFlowId(200L)).thenReturn(List.of());
        when(connectionRepository.findAllByFlowId(200L)).thenReturn(List.of());

        RoomTemplateDetailResponse response = flowService.getTemplateFlowDetail(200L);

        assertThat(response.templateName()).isEqualTo("Flow 200");
    }

    @Test
    @DisplayName("대상이 일반 플로우임 -> InvalidFlowException")
    void getTemplateFlowDetail_failsWhenNotTemplate() {
        Flow mockFlow = createMockFlow(200L, false); // isTemplate = false
        when(flowRepository.findById(200L)).thenReturn(Optional.of(mockFlow));

        assertThatThrownBy(() -> flowService.getTemplateFlowDetail(200L))
                .isInstanceOf(InvalidFlowException.class);
    }

    @Test
    @DisplayName("Flow 수정")
    void updateFlow_success() {
        NodeInfo node1 = createConditionNode(1L);
        NodeInfo node2 = createActionNode(2L);
        ConnectionInfo conn = new ConnectionInfo(1L, 2L, BranchType.TRUE);
        FlowUpdateRequest request = new FlowUpdateRequest("updated flow", "desc", true, List.of(node1, node2), List.of(conn));

        Flow mockFlow = createMockFlow(100L, false);
        when(flowRepository.findByIdAndRoomId(100L, 1L)).thenReturn(Optional.of(mockFlow));

        Node mockNode = mock(Node.class);
        when(nodeRepository.save(any(Node.class))).thenReturn(mockNode);
        when(nodeRepository.getReferenceById(anyLong())).thenReturn(mockNode);

        flowService.updateFlow(1L, 100L, request);

        verify(mockFlow).updateRegular("updated flow", "desc", true);
        verify(connectionRepository).deleteAllByFlowId(100L);
        verify(nodeRepository).deleteAllByFlowId(100L);
        verify(nodeRepository, times(2)).save(any(Node.class));
        verify(connectionRepository).saveAll(anyList());
        verify(flowCacheRepository).evict(1L);
    }

    @Test
    @DisplayName("Flow 삭제")
    void deleteFlow_success() {
        Flow mockFlow = mock(Flow.class);
        when(mockFlow.getIsTemplate()).thenReturn(false);
        when(flowRepository.findByIdAndRoomId(1L, 100L)).thenReturn(Optional.of(mockFlow));

        flowService.deleteFlow(100L, 1L);

        verify(flowRepository).deleteById(1L);
        verify(flowCacheRepository).evict(100L);
    }

    @Test
    @DisplayName("존재하지 않음 -> UnauthorizedFlowAccessException")
    void deleteFlow_failsWhenNotExists() {

        assertThatThrownBy(() -> flowService.deleteFlow(1L, 100L)).isInstanceOf(UnauthorizedFlowAccessException.class);
    }

    @Test
    @DisplayName("템플릿 Flow 삭제 -> InvalidFlowException")
    void deleteFlow_failsWhenTemplate() {
        Flow mockFlow = mock(Flow.class);
        when(mockFlow.getIsTemplate()).thenReturn(true);
        when(flowRepository.findByIdAndRoomId(1L, 100L)).thenReturn(Optional.of(mockFlow));

        assertThatThrownBy(() -> flowService.deleteFlow(100L, 1L)).isInstanceOf(InvalidFlowException.class);
    }

    @Test
    @DisplayName("플로우 비활성화")
    void updateFlowStatus_isActive_false(){

        UpdateFlowStatusRequest request = new UpdateFlowStatusRequest(false);
        Flow mockFlow = mock(Flow.class);

        when(flowRepository.findByIdAndRoomId(1L, 100L)).thenReturn(Optional.of(mockFlow));

        flowService.updateStatus(100L, 1L, request);

        verify(flowRepository).findByIdAndRoomId(1L, 100L);
        verify(mockFlow).updateStatus(false);

    }
}
