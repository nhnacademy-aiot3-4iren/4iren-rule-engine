package com.nhnacademy.ruleengine.domain.flow.service.impl;

import com.nhnacademy.ruleengine.common.cache.repository.FlowCacheRepository;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidConnectionException;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.unauthorized.UnauthorizedFlowAccessException;
import com.nhnacademy.ruleengine.common.external.service.SensorStaticMetaService;
import com.nhnacademy.ruleengine.domain.flow.dto.*;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.flow.validator.FlowValidator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.*;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import com.nhnacademy.ruleengine.domain.templateflow.repository.FlowTemplateMeasurementTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowServiceImplTest {

    @Mock
    private FlowRepository flowRepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private ConnectionRepository connectionRepository;
    @Mock
    private FlowTemplateMeasurementTypeRepository flowTemplateMeasurementTypeRepository;
    @Mock
    private SensorStaticMetaService metaService;
    @Mock
    private FlowCacheRepository flowCacheRepository;
    @Mock
    private FlowValidator flowValidator; // Mock으로 제어

    @InjectMocks
    private FlowServiceImpl flowService;

    private static final Long ROOM_ID = 1L;
    private static final Long FLOW_ID = 10L;


    private Flow createRegularFlow(Long roomId, String flowName, boolean isActive) {
        Flow flow = Flow.regularBuilder()
                .roomId(roomId)
                .flowName(flowName)
                .isActive(isActive)
                .description("테스트 설명")
                .build();
        setField(flow, "id", FLOW_ID);
        setField(flow, "createdAt", LocalDateTime.now());
        setField(flow, "updatedAt", LocalDateTime.now());
        return flow;
    }

    private Flow createTemplateFlow(Long id, String flowName) {
        Flow flow = Flow.templateBuilder()
                .flowName(flowName)
                .description("템플릿 설명")
                .build();
        setField(flow, "id", id);
        setField(flow, "createdAt", LocalDateTime.now());
        setField(flow, "updatedAt", LocalDateTime.now());
        return flow;
    }

    private Node createNode(Long id, Flow flow, NodeType nodeType, NodeConfig config) {
        Node node = Node.builder()
                .flow(flow)
                .nodeName("노드" + id)
                .nodeType(nodeType)
                .nodeConfig(config)
                .cooldownSec(0)
                .build();
        setField(node, "id", id);
        return node;
    }

    private NodeInfo nodeInfo(Long tempId, NodeType nodeType, NodeConfig config) {
        return new NodeInfo(tempId, "노드" + tempId, nodeType, config, 0);
    }

    private ConnectionInfo connectionInfo(Long sourceId, Long targetId) {
        return new ConnectionInfo(sourceId, targetId, BranchType.TRUE);
    }

    private ThresholdNodeConfig thresholdConfig() {
        return new ThresholdNodeConfig(
                NodeType.THRESHOLD, 0, 0,
                MeasurementType.TEMPERATURE,
                "°C", Operator.GT, 25.0
        );
    }

    private AlertNodeConfig alertConfig() {
        return new AlertNodeConfig(
                NodeType.ALERT, 0, 0,
                AlertChannel.TELEGRAM,
                "온도 초과 알람",
                AlertType.COMFORT_LIMIT_EXCEEDED
        );
    }

    private List<NodeInfo> validNodes() {
        return List.of(
                nodeInfo(-1L, NodeType.THRESHOLD, thresholdConfig()),
                nodeInfo(-2L, NodeType.ALERT, alertConfig())
        );
    }

    private List<ConnectionInfo> validConnections() {
        return List.of(connectionInfo(-1L, -2L));
    }

    private void stubNodeSave() {
        long[] idSeq = {100L};
        when(nodeRepository.save(any(Node.class))).thenAnswer(inv -> {
            Node node = inv.getArgument(0);
            Node saved = Node.builder()
                    .flow(node.getFlow())
                    .nodeName(node.getNodeName())
                    .nodeType(node.getNodeType())
                    .nodeConfig(node.getNodeConfig())
                    .cooldownSec(node.getCooldownSec())
                    .build();
            setField(saved, "id", idSeq[0]++);
            return saved;
        });
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Nested
    @DisplayName("createFlow")
    class CreateFlow {

        @Test
        @DisplayName("정상 생성 - FlowCreateResponse 반환")
        void createFlow_success_returnsResponse() {
            // given
            FlowCreateRequest request = new FlowCreateRequest(
                    "테스트 플로우", "설명", validNodes(), validConnections()
            );
            Flow savedFlow = createRegularFlow(ROOM_ID, "테스트 플로우", true);

            // flowValidator.validate()는 아무것도 안함 (정상 케이스)
            doNothing().when(flowValidator).validate(anyList(), anyList());
            when(flowRepository.save(any(Flow.class))).thenReturn(savedFlow);
            stubNodeSave();

            // when
            FlowCreateResponse response = flowService.createFlow(ROOM_ID, request);

            // then
            assertThat(response.flowId()).isEqualTo(FLOW_ID);
            verify(flowValidator).validate(request.nodes(), request.connections());
            verify(flowRepository).save(any(Flow.class));
            verify(nodeRepository, times(2)).save(any(Node.class));
            verify(connectionRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("validate 실패 - FlowValidationFailed 전파, flowRepository.save 호출 안됨")
        void createFlow_validationFailed_doesNotSaveFlow() {
            // given
            FlowCreateRequest request = new FlowCreateRequest(
                    "플로우", "설명", validNodes(), validConnections()
            );

            // flowValidator가 예외 던짐
            doThrow(new FlowValidationFailed(List.of("노드는 최소 2개 이상이어야 합니다.")))
                    .when(flowValidator).validate(anyList(), anyList());

            // when & then
            assertThatThrownBy(() -> flowService.createFlow(ROOM_ID, request))
                    .isInstanceOf(FlowValidationFailed.class);

            // validate 실패 시 save 호출 안됨
            verify(flowRepository, never()).save(any(Flow.class));
            verify(nodeRepository, never()).save(any(Node.class));
            verify(connectionRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("커넥션 sourceNodeId 없음 - InvalidConnectionException")
        void createFlow_invalidConnectionSourceId_throwsException() {
            // given
            List<NodeInfo> nodes = validNodes();
            List<ConnectionInfo> connections = List.of(connectionInfo(-99L, -2L)); // -99L 없음
            FlowCreateRequest request = new FlowCreateRequest("플로우", "설명", nodes, connections);

            doNothing().when(flowValidator).validate(anyList(), anyList());
            Flow savedFlow = createRegularFlow(ROOM_ID, "플로우", true);
            when(flowRepository.save(any(Flow.class))).thenReturn(savedFlow);
            stubNodeSave();

            // when & then
            assertThatThrownBy(() -> flowService.createFlow(ROOM_ID, request))
                    .isInstanceOf(InvalidConnectionException.class);

            verify(connectionRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("커넥션 빈 리스트 - saveConnections 호출 안됨")
        void createFlow_emptyConnections_doesNotSaveConnections() {
            // given
            FlowCreateRequest request = new FlowCreateRequest(
                    "플로우", "설명", validNodes(), List.of()
            );
            doNothing().when(flowValidator).validate(anyList(), anyList());
            Flow savedFlow = createRegularFlow(ROOM_ID, "플로우", true);
            when(flowRepository.save(any(Flow.class))).thenReturn(savedFlow);
            stubNodeSave();

            // when
            flowService.createFlow(ROOM_ID, request);

            // then - connections 비어있으면 saveAll 호출 안됨
            verify(connectionRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("getFlowList")
    class GetFlowList {

        @Test
        @DisplayName("플로우 존재 - 리스트 반환")
        void getFlowList_exists_returnsList() {
            // given
            Flow flow = createRegularFlow(ROOM_ID, "플로우1", true);
            when(flowRepository.findAllByRoomId(ROOM_ID)).thenReturn(List.of(flow));

            // when
            FlowListResponse response = flowService.getFlowList(ROOM_ID);

            // then
            assertThat(response.flowResponseList()).hasSize(1);
            assertThat(response.flowResponseList().getFirst().flowName()).isEqualTo("플로우1");
        }

        @Test
        @DisplayName("플로우 없음 - 빈 리스트 반환")
        void getFlowList_empty_returnsEmptyList() {
            // given
            when(flowRepository.findAllByRoomId(ROOM_ID)).thenReturn(List.of());

            // when
            FlowListResponse response = flowService.getFlowList(ROOM_ID);

            // then
            assertThat(response.flowResponseList()).isEmpty();
        }

        @Test
        @DisplayName("여러 플로우 - 전체 반환")
        void getFlowList_multipleFlows_returnsAll() {
            // given
            Flow flow1 = createRegularFlow(ROOM_ID, "플로우1", true);
            Flow flow2 = createRegularFlow(ROOM_ID, "플로우2", false);
            setField(flow2, "id", 20L);
            when(flowRepository.findAllByRoomId(ROOM_ID)).thenReturn(List.of(flow1, flow2));

            // when
            FlowListResponse response = flowService.getFlowList(ROOM_ID);

            // then
            assertThat(response.flowResponseList()).hasSize(2);
        }
    }


    @Nested
    @DisplayName("getFlowDetail")
    class GetFlowDetail {

        @Test
        @DisplayName("정상 조회 - FlowDetailResponse 반환")
        void getFlowDetail_success_returnsDetail() {
            // given
            Flow flow = createRegularFlow(ROOM_ID, "플로우", true);
            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(flow));
            when(nodeRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of());
            when(connectionRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of());

            // when
            FlowDetailResponse response = flowService.getFlowDetail(ROOM_ID, FLOW_ID);

            // then
            assertThat(response.flowId()).isEqualTo(FLOW_ID);
            assertThat(response.roomId()).isEqualTo(ROOM_ID);
            assertThat(response.flowName()).isEqualTo("플로우");
            assertThat(response.isActive()).isTrue();
        }

        @Test
        @DisplayName("노드/커넥션 포함 조회 - 정상 매핑")
        void getFlowDetail_withNodesAndConnections_returnsMappedDetail() {
            // given
            Flow flow = createRegularFlow(ROOM_ID, "플로우", true);
            Node node1 = createNode(1L, flow, NodeType.THRESHOLD, thresholdConfig());
            Node node2 = createNode(2L, flow, NodeType.ALERT, alertConfig());
            Connection connection = Connection.create(flow, node1, node2, "TRUE");
            setField(connection, "id", 1L);

            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(flow));
            when(nodeRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of(node1, node2));
            when(connectionRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of(connection));

            // when
            FlowDetailResponse response = flowService.getFlowDetail(ROOM_ID, FLOW_ID);

            // then
            assertThat(response.nodes()).hasSize(2);
            assertThat(response.connections()).hasSize(1);
            assertThat(response.connections().getFirst().branchType()).isEqualTo(BranchType.TRUE);
        }

        @Test
        @DisplayName("플로우 없음 - FlowNotFoundException")
        void getFlowDetail_notFound_throwsException() {
            // given
            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> flowService.getFlowDetail(ROOM_ID, FLOW_ID))
                    .isInstanceOf(FlowNotFoundException.class);
        }

        @Test
        @DisplayName("템플릿 플로우 조회 시도 - InvalidFlowException, 노드/커넥션 조회 안됨")
        void getFlowDetail_templateFlow_throwsException() {
            // given
            Flow templateFlow = createTemplateFlow(FLOW_ID, "템플릿");
            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(templateFlow));

            // when & then
            assertThatThrownBy(() -> flowService.getFlowDetail(ROOM_ID, FLOW_ID))
                    .isInstanceOf(InvalidFlowException.class);

            verify(nodeRepository, never()).findAllByFlowId(anyLong());
            verify(connectionRepository, never()).findAllByFlowId(anyLong());
        }
    }

    // ───────────────────────────────────────────
    // getFlowTemplateList
    // ───────────────────────────────────────────

    @Nested
    @DisplayName("getFlowTemplateList")
    class GetFlowTemplateList {

        @Test
        @DisplayName("강의실 센서로 사용 가능한 템플릿만 필터링")
        void getFlowTemplateList_filtersAvailableTemplates() {
            // given
            Flow template1 = createTemplateFlow(1L, "온도 템플릿");
            Flow template2 = createTemplateFlow(2L, "CO2 템플릿");

            FlowTemplateMeasurementType type1 = FlowTemplateMeasurementType.builder()
                    .flow(template1).measurementType(MeasurementType.TEMPERATURE).build();
            FlowTemplateMeasurementType type2 = FlowTemplateMeasurementType.builder()
                    .flow(template2).measurementType(MeasurementType.CO2).build();

            when(flowRepository.findAllByIsTemplate(true)).thenReturn(List.of(template1, template2));
            when(flowTemplateMeasurementTypeRepository.findAllByFlowIn(anyList()))
                    .thenReturn(List.of(type1, type2));
            when(metaService.getMeasurementTypeOptionsInRoom(ROOM_ID))
                    .thenReturn(List.of(MeasurementType.TEMPERATURE));
            when(flowRepository.findAllById(List.of(1L))).thenReturn(List.of(template1));

            // when
            RoomTemplateListResponse response = flowService.getFlowTemplateList(ROOM_ID);

            // then
            assertThat(response.roomTemplateResponseList()).hasSize(1);
            assertThat(response.roomTemplateResponseList().getFirst().templateName())
                    .isEqualTo("온도 템플릿");
        }

        @Test
        @DisplayName("강의실 센서가 모든 템플릿 요구사항 충족 - 전체 반환")
        void getFlowTemplateList_allSensorsAvailable_returnsAll() {
            // given
            Flow template1 = createTemplateFlow(1L, "온도 템플릿");
            Flow template2 = createTemplateFlow(2L, "CO2 템플릿");

            FlowTemplateMeasurementType type1 = FlowTemplateMeasurementType.builder()
                    .flow(template1).measurementType(MeasurementType.TEMPERATURE).build();
            FlowTemplateMeasurementType type2 = FlowTemplateMeasurementType.builder()
                    .flow(template2).measurementType(MeasurementType.CO2).build();

            when(flowRepository.findAllByIsTemplate(true)).thenReturn(List.of(template1, template2));
            when(flowTemplateMeasurementTypeRepository.findAllByFlowIn(anyList()))
                    .thenReturn(List.of(type1, type2));
            when(metaService.getMeasurementTypeOptionsInRoom(ROOM_ID))
                    .thenReturn(List.of(MeasurementType.TEMPERATURE, MeasurementType.CO2));
            when(flowRepository.findAllById(anyList())).thenReturn(List.of(template1, template2));

            // when
            RoomTemplateListResponse response = flowService.getFlowTemplateList(ROOM_ID);

            // then
            assertThat(response.roomTemplateResponseList()).hasSize(2);
        }

        @Test
        @DisplayName("강의실 센서 없음 - 빈 리스트 반환")
        void getFlowTemplateList_noSensorsInRoom_returnsEmpty() {
            // given
            Flow template1 = createTemplateFlow(1L, "온도 템플릿");
            FlowTemplateMeasurementType type1 = FlowTemplateMeasurementType.builder()
                    .flow(template1).measurementType(MeasurementType.TEMPERATURE).build();

            when(flowRepository.findAllByIsTemplate(true)).thenReturn(List.of(template1));
            when(flowTemplateMeasurementTypeRepository.findAllByFlowIn(anyList()))
                    .thenReturn(List.of(type1));
            when(metaService.getMeasurementTypeOptionsInRoom(ROOM_ID)).thenReturn(List.of());
            when(flowRepository.findAllById(List.of())).thenReturn(List.of());

            // when
            RoomTemplateListResponse response = flowService.getFlowTemplateList(ROOM_ID);

            // then
            assertThat(response.roomTemplateResponseList()).isEmpty();
        }

        @Test
        @DisplayName("복수 MeasurementType 요구 템플릿 - 일부만 충족 시 제외")
        void getFlowTemplateList_partialSensorMatch_excluded() {
            // given
            Flow template1 = createTemplateFlow(1L, "온습도 템플릿");

            FlowTemplateMeasurementType type1 = FlowTemplateMeasurementType.builder()
                    .flow(template1).measurementType(MeasurementType.TEMPERATURE).build();
            FlowTemplateMeasurementType type2 = FlowTemplateMeasurementType.builder()
                    .flow(template1).measurementType(MeasurementType.HUMIDITY).build();

            when(flowRepository.findAllByIsTemplate(true)).thenReturn(List.of(template1));
            when(flowTemplateMeasurementTypeRepository.findAllByFlowIn(anyList()))
                    .thenReturn(List.of(type1, type2));
            // TEMPERATURE만 있고 HUMIDITY 없음 → 제외
            when(metaService.getMeasurementTypeOptionsInRoom(ROOM_ID))
                    .thenReturn(List.of(MeasurementType.TEMPERATURE));
            when(flowRepository.findAllById(List.of())).thenReturn(List.of());

            // when
            RoomTemplateListResponse response = flowService.getFlowTemplateList(ROOM_ID);

            // then
            assertThat(response.roomTemplateResponseList()).isEmpty();
        }
    }

    // ───────────────────────────────────────────
    // getTemplateFlowDetail
    // ───────────────────────────────────────────

    @Nested
    @DisplayName("getTemplateFlowDetail")
    class GetTemplateFlowDetail {

        @Test
        @DisplayName("정상 조회 - RoomTemplateDetailResponse 반환")
        void getTemplateFlowDetail_success_returnsDetail() {
            // given
            Flow templateFlow = createTemplateFlow(FLOW_ID, "온도 템플릿");
            when(flowRepository.findById(FLOW_ID)).thenReturn(Optional.of(templateFlow));
            when(nodeRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of());
            when(connectionRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of());

            // when
            RoomTemplateDetailResponse response = flowService.getTemplateFlowDetail(FLOW_ID);

            // then
            assertThat(response.templateName()).isEqualTo("온도 템플릿");
        }

        @Test
        @DisplayName("노드/커넥션 포함 조회 - 정상 매핑")
        void getTemplateFlowDetail_withNodesAndConnections_returnsMappedDetail() {
            // given
            Flow templateFlow = createTemplateFlow(FLOW_ID, "온도 템플릿");
            Node node1 = createNode(1L, templateFlow, NodeType.THRESHOLD, thresholdConfig());
            Node node2 = createNode(2L, templateFlow, NodeType.ALERT, alertConfig());
            Connection connection = Connection.create(templateFlow, node1, node2, "TRUE");
            setField(connection, "id", 1L);

            when(flowRepository.findById(FLOW_ID)).thenReturn(Optional.of(templateFlow));
            when(nodeRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of(node1, node2));
            when(connectionRepository.findAllByFlowId(FLOW_ID)).thenReturn(List.of(connection));

            // when
            RoomTemplateDetailResponse response = flowService.getTemplateFlowDetail(FLOW_ID);

            // then
            assertThat(response.nodes()).hasSize(2);
            assertThat(response.connections()).hasSize(1);
        }

        @Test
        @DisplayName("플로우 없음 - FlowNotFoundException")
        void getTemplateFlowDetail_notFound_throwsException() {
            // given
            when(flowRepository.findById(FLOW_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> flowService.getTemplateFlowDetail(FLOW_ID))
                    .isInstanceOf(FlowNotFoundException.class);
        }

        @Test
        @DisplayName("일반 플로우로 조회 시도 - InvalidFlowException, 노드/커넥션 조회 안됨")
        void getTemplateFlowDetail_regularFlow_throwsException() {
            // given
            Flow regularFlow = createRegularFlow(ROOM_ID, "일반 플로우", true);
            when(flowRepository.findById(FLOW_ID)).thenReturn(Optional.of(regularFlow));

            // when & then
            assertThatThrownBy(() -> flowService.getTemplateFlowDetail(FLOW_ID))
                    .isInstanceOf(InvalidFlowException.class);

            verify(nodeRepository, never()).findAllByFlowId(anyLong());
            verify(connectionRepository, never()).findAllByFlowId(anyLong());
        }
    }

    // ───────────────────────────────────────────
    // updateFlow
    // ───────────────────────────────────────────

    @Nested
    @DisplayName("updateFlow")
    class UpdateFlow {

        @Test
        @DisplayName("정상 업데이트 - 노드/커넥션 재생성 및 캐시 무효화")
        void updateFlow_success_updatesAndEvictsCache() {
            // given
            Flow flow = createRegularFlow(ROOM_ID, "기존 플로우", true);
            FlowUpdateRequest request = new FlowUpdateRequest(
                    "수정된 플로우", "수정된 설명", true, validNodes(), validConnections()
            );

            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(flow));
            doNothing().when(flowValidator).validate(anyList(), anyList());
            stubNodeSave();

            // when
            flowService.updateFlow(ROOM_ID, FLOW_ID, request);

            // then
            verify(connectionRepository).deleteAllByFlowId(FLOW_ID);
            verify(nodeRepository).deleteAllByFlowId(FLOW_ID);
            verify(nodeRepository, times(2)).save(any(Node.class));
            verify(connectionRepository).saveAll(anyList());
            verify(flowValidator).validate(request.nodes(), request.connections());
            verify(flowCacheRepository).evict(ROOM_ID);
        }

        @Test
        @DisplayName("flow.updateRegular 호출 확인 - 필드 변경 검증")
        void updateFlow_callsUpdateRegular_fieldsChanged() {
            // given
            Flow flow = spy(createRegularFlow(ROOM_ID, "기존 플로우", true));
            FlowUpdateRequest request = new FlowUpdateRequest(
                    "수정된 플로우", "수정된 설명", false, validNodes(), validConnections()
            );

            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(flow));
            doNothing().when(flowValidator).validate(anyList(), anyList());
            stubNodeSave();

            // when
            flowService.updateFlow(ROOM_ID, FLOW_ID, request);

            // then
            verify(flow).updateRegular("수정된 플로우", "수정된 설명", false);
            assertThat(flow.getFlowName()).isEqualTo("수정된 플로우");
            assertThat(flow.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("플로우 없음 - FlowNotFoundException, 캐시 무효화 안됨")
        void updateFlow_notFound_throwsException() {
            // given
            FlowUpdateRequest request = new FlowUpdateRequest(
                    "수정", "설명", true, validNodes(), validConnections()
            );
            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> flowService.updateFlow(ROOM_ID, FLOW_ID, request))
                    .isInstanceOf(FlowNotFoundException.class);

            verify(flowCacheRepository, never()).evict(anyLong());
        }

        @Test
        @DisplayName("validate 실패 - FlowValidationFailed, 캐시 무효화 안됨")
        void updateFlow_validationFailed_doesNotEvictCache() {
            // given
            Flow flow = createRegularFlow(ROOM_ID, "플로우", true);
            FlowUpdateRequest request = new FlowUpdateRequest(
                    "수정", "설명", true, validNodes(), validConnections()
            );

            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(flow));
            stubNodeSave();
            doThrow(new FlowValidationFailed(List.of("노드는 최소 2개 이상이어야 합니다.")))
                    .when(flowValidator).validate(anyList(), anyList());

            // when & then
            assertThatThrownBy(() -> flowService.updateFlow(ROOM_ID, FLOW_ID, request))
                    .isInstanceOf(FlowValidationFailed.class);

            verify(flowCacheRepository, never()).evict(anyLong());
        }
    }

    // ───────────────────────────────────────────
    // deleteFlow
    // ───────────────────────────────────────────

    @Nested
    @DisplayName("deleteFlow")
    class DeleteFlow {

        @Test
        @DisplayName("정상 삭제 - deleteById 및 캐시 무효화 호출")
        void deleteFlow_success_deletesAndEvictsCache() {
            // given
            Flow flow = createRegularFlow(ROOM_ID, "플로우", true);
            when(flowRepository.existsByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(true);
            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(flow));

            // when
            flowService.deleteFlow(ROOM_ID, FLOW_ID);

            // then
            verify(flowRepository).deleteById(FLOW_ID);
            verify(flowCacheRepository).evict(ROOM_ID);
        }

        @Test
        @DisplayName("접근 권한 없음 - UnauthorizedFlowAccessException, deleteById/캐시 무효화 안됨")
        void deleteFlow_unauthorized_throwsException() {
            // given
            when(flowRepository.existsByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> flowService.deleteFlow(ROOM_ID, FLOW_ID))
                    .isInstanceOf(UnauthorizedFlowAccessException.class);

            verify(flowRepository, never()).deleteById(anyLong());
            verify(flowCacheRepository, never()).evict(anyLong());
        }

        @Test
        @DisplayName("템플릿 플로우 삭제 시도 - InvalidFlowException, deleteById/캐시 무효화 안됨")
        void deleteFlow_templateFlow_throwsException() {
            // given
            Flow templateFlow = createTemplateFlow(FLOW_ID, "템플릿");
            when(flowRepository.existsByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(true);
            when(flowRepository.findByIdAndRoomId(FLOW_ID, ROOM_ID)).thenReturn(Optional.of(templateFlow));

            // when & then
            assertThatThrownBy(() -> flowService.deleteFlow(ROOM_ID, FLOW_ID))
                    .isInstanceOf(InvalidFlowException.class);

            verify(flowRepository, never()).deleteById(anyLong());
            verify(flowCacheRepository, never()).evict(anyLong());
        }
    }
}