package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.common.cache.repository.FlowCacheRepository;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.repository.ConnectionRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flow.repository.NodeRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowLoaderTest {

    @Mock
    private FlowRepository flowRepository;
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private ConnectionRepository connectionRepository;
    @Mock
    private FlowScheduleRepository flowScheduleRepository;
    @Mock
    private FlowGraphBuilder flowGraphBuilder;

    @Mock
    private FlowCacheRepository flowCacheRepository;
    @InjectMocks
    private FlowLoader flowLoader;

    private final Long ROOM_ID = 1L;

    @Test
    @DisplayName("캐시 히트 - DB 조회 없이 캐시 반환")
    void load_cacheHit() {

        ExecutableFlow cachedFlow = mockExecutableFlow(1L);
        when(flowCacheRepository.get(ROOM_ID)).thenReturn(List.of(cachedFlow));
        when(cachedFlow.flowId()).thenReturn(1L);

        List<ExecutableFlow> result = flowLoader.load(ROOM_ID);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().flowId()).isEqualTo(1L);

        //DB 접근 없음
        verifyNoInteractions(flowRepository);
        verifyNoInteractions(nodeRepository);
        verifyNoInteractions(connectionRepository);
        verifyNoInteractions(flowScheduleRepository);
        verifyNoInteractions(flowGraphBuilder);

        //캐시 저장 없음
        verify(flowCacheRepository, never()).set(anyLong(), anyList());
    }

    @Test
    @DisplayName("캐시 미스 - DB에서 플로우 조회 후 반환")
    void load_cacheMiss(){
        Flow flow = mockFlow(1L);
        Node node = mockNode(1L);
        Connection conn = mockConnection(1L);
        FlowSchedule schedule = mockSchedule(1L);
        ExecutableFlow executableFlow = mockExecutableFlow(1L);
        when(executableFlow.flowId()).thenReturn(1L);


        when(flowCacheRepository.get(ROOM_ID)).thenReturn(null);
        when(flowRepository.findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse(ROOM_ID)).thenReturn(List.of(flow));
        when(nodeRepository.findAllByFlowIdIn(List.of(1L))).thenReturn(List.of(node));
        when(connectionRepository.findAllByFlowIdIn(List.of(1L))).thenReturn(List.of(conn));
        when(flowScheduleRepository.findAllByFlowIdIn(List.of(1L))).thenReturn(List.of(schedule));
        when(flowGraphBuilder.build(eq(flow),anyList(), anyList(), anyList())).thenReturn(executableFlow);

        List<ExecutableFlow> result = flowLoader.load(ROOM_ID);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().flowId()).isEqualTo(1L);

        //DB조회후 레디스에 저장
        verify(flowCacheRepository).set(eq(ROOM_ID), anyList());
    }
    @Test
    @DisplayName("캐시 미스 - 노드/커넥션/스케줄이 플로우별로 올바르게 그루핑되어 전달됨")
    void load_cacheMiss_groupsByFlowId_correctly() {
        Flow flow1 = mockFlow(1L);
        Flow flow2 = mockFlow(2L);

        Node node1 = mockNode(1L);
        Node node2 = mockNode(2L);
        Connection conn1 = mockConnection(1L);
        Connection conn2 = mockConnection(2L);

        ExecutableFlow executableFlow = mockExecutableFlow(1L);


        when(flowCacheRepository.get(ROOM_ID)).thenReturn(null);
        when(flowRepository.findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse(ROOM_ID))
                .thenReturn(List.of(flow1, flow2));
        when(nodeRepository.findAllByFlowIdIn(anyList())).thenReturn(List.of(node1, node2));
        when(connectionRepository.findAllByFlowIdIn(anyList())).thenReturn(List.of(conn1, conn2));
        when(flowScheduleRepository.findAllByFlowIdIn(anyList())).thenReturn(List.of());
    when(flowGraphBuilder.build(any(), anyList(), anyList(), anyList()))
                .thenReturn(executableFlow);


        flowLoader.load(ROOM_ID);

        verify(flowGraphBuilder).build(
                eq(flow1),
                argThat(nodes -> nodes.size() == 1 && nodes.contains(node1)),
                argThat(conns -> conns.size() == 1 && conns.contains(conn1)),
                anyList()
        );
        verify(flowGraphBuilder).build(
                eq(flow2),
                argThat(nodes -> nodes.size() == 1 && nodes.contains(node2)),
                argThat(connections -> connections.size() == 1 && connections.contains(conn2)),
                anyList()
        );

    }

    @Test
    @DisplayName("활성 플로우 없음 ")
    void load_noActiveFlows_returnEmptyList(){
        when(flowCacheRepository.get(ROOM_ID)).thenReturn(null);
        when(flowRepository.findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse(ROOM_ID)).thenReturn(List.of());

        List<ExecutableFlow> result = flowLoader.load(ROOM_ID);

        //빈 리스트 반환
        assertThat(result).isEmpty();

        //캐시 저장 안함
        verify(flowCacheRepository, never()).set(anyLong(), anyList());

        //노드/커넥션/ 스케줄 db 조회 안함
        verifyNoInteractions(nodeRepository);
        verifyNoInteractions(connectionRepository);
        verifyNoInteractions(flowScheduleRepository);
    }


    private Flow mockFlow(Long flowId){
        Flow flow = mock(Flow.class);
        when(flow.getId()).thenReturn(flowId);
        return flow;
    }

    private Node mockNode(Long flowId){
        Node node = mock(Node.class);
        Flow flow = mock(Flow.class);
        when(flow.getId()).thenReturn(flowId);
        when(node.getFlow()).thenReturn(flow);
        return node;
    }

    private Connection mockConnection(Long flowId) {
        Connection conn = mock(Connection.class);
        Flow flow = mock(Flow.class);
        when(flow.getId()).thenReturn(flowId);
        when(conn.getFlow()).thenReturn(flow);
        return conn;
    }

    private FlowSchedule mockSchedule(Long flowId) {
        FlowSchedule schedule = mock(FlowSchedule.class);
        Flow flow = mock(Flow.class);
        when(flow.getId()).thenReturn(flowId);
        when(schedule.getFlow()).thenReturn(flow);
        return schedule;
    }
    private ExecutableFlow mockExecutableFlow(Long flowId) {
        ExecutableFlow executableFlow = mock(ExecutableFlow.class);
        return executableFlow;
    }


}