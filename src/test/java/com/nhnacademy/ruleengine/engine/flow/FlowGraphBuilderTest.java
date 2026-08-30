package com.nhnacademy.ruleengine.engine.flow;

import com.nhnacademy.ruleengine.common.exception.invalid.InvalidFlowException;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.start.StartNodeConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlowGraphBuilderTest {

    private FlowGraphBuilder flowGraphBuilder;

    private Flow flow;
    @BeforeEach
    void setUp() {
        flowGraphBuilder = new FlowGraphBuilder();

        flow = mock(Flow.class);
        when(flow.getId()).thenReturn(1L);
        when(flow.getFlowName()).thenReturn("테스트 플로우");
        when(flow.getRoomId()).thenReturn(1L);
    }

    @Test
    @DisplayName("플로우를 ExecutableFlow로 변환")
    void build() {

        Node node1 = mockStartNode(1L);
        Node node2 = mockNode(2L);
        Connection conn = mockConnection(node1, node2, "TRUE");

        ExecutableFlow result = flowGraphBuilder.build(
                flow,
                List.of(node1, node2),
                List.of(conn),
                List.of()
        );

        assertNotNull(result);
        assertEquals(result.flowId(),1L);
        assertEquals(result.flowName(), "테스트 플로우");
        assertEquals(result.roomId(), 1L);

        //nodeMap 노드 정보 확인
        assertEquals(result.nodeMap().size(), 2);
        assertEquals(result.startNodeId(), 1L);
        assertTrue(result.nodeMap().containsKey(1L));
        assertTrue(result.nodeMap().containsKey(2L));
        assertEquals(result.nodeMap().get(1L).nodeId(), 1L);
        assertEquals(result.nodeMap().get(2L).nodeId(), 2L);

        //인접 맵 정보 확인
        assertTrue(result.trueAdjacencyMap().get(1L).contains(2L));
        assertTrue(result.falseAdjacencyMap().get(1L).isEmpty());

        assertTrue(result.schedules().isEmpty());

    }

    @Test
    @DisplayName("TRUE/FALSE 커넥션 동시 존재 - 각각 올바른 맵에 저장됨 + 대소문자 확인")
    void build_bothBranchTypes() {

        Node node1 = mockStartNode(1L);
        Node node2 = mockNode(2L);
        Node node3 = mockNode(3L);
        Connection trueConn = mockConnection(node1, node2, "true");
        Connection falseConn = mockConnection(node1, node3, "FALSE");

        ExecutableFlow result = flowGraphBuilder.build(
                flow,
                List.of(node1, node2, node3),
                List.of(trueConn, falseConn),
                List.of()
        );

        assertTrue(result.trueAdjacencyMap().get(1L).contains(2L));
        assertTrue(result.falseAdjacencyMap().get(1L).contains(3L));
    }

    @Test
    @DisplayName("잘못된 branchType - InvalidFlowException 발생")
    void build_invalidBranchType_throwsException() {
        Node node1 = mockNode(1L);
        Node node2 = mockNode(2L);
        Connection conn = mockConnection(node1, node2, "INVALID");


        assertThrows(InvalidFlowException.class,()->{
            flowGraphBuilder.build(
                    flow,
                    List.of(node1, node2),
                    List.of(conn),
                    List.of()
            );
        });
    }

    @Test
    @DisplayName("스케줄 있을 때 - executableSchedules에 올바르게 담김")
    void build_withSchedules() {
        Node node1 = mockStartNode(1L);
        FlowSchedule schedule = mockSchedule(DayOfWeek.MONDAY, LocalTime.of(9,0), LocalTime.of(12, 0));

        ExecutableFlow result = flowGraphBuilder.build(flow, List.of(node1), List.of(), List.of(schedule));

        assertEquals(result.schedules().size(), 1);
        assertEquals(result.schedules().getFirst().dayOfWeek(), DayOfWeek.MONDAY);
        assertEquals(result.schedules().getFirst().startTime(), LocalTime.of(9,0));
        assertEquals(result.schedules().getFirst().endTime(), LocalTime.of(12, 0));
    }

        private Node mockStartNode(Long id){
            Node node = mock(Node.class);
            when(node.getId()).thenReturn(id);
            when(node.getNodeName()).thenReturn("node-" + id);
            when(node.getNodeType()).thenReturn(NodeType.START);
            when(node.getNodeConfig()).thenReturn(mock(StartNodeConfig.class));
            when(node.getCooldownSec()).thenReturn(0);
            return node;
        }

        private Node mockNode(Long id){
        Node node = mock(Node.class);
        when(node.getId()).thenReturn(id);
        when(node.getNodeName()).thenReturn("node-" + id);
        when(node.getNodeType()).thenReturn(NodeType.THRESHOLD);
        when(node.getNodeConfig()).thenReturn(mock(ThresholdNodeConfig.class));
        when(node.getCooldownSec()).thenReturn(0);
        return node;
    }

    private Connection mockConnection(Node source, Node target, String branchType){
        Connection connection = mock(Connection.class);
        when(connection.getSourceNode()).thenReturn(source);
        when(connection.getTargetNode()).thenReturn(target);
        when(connection.getBranchType()).thenReturn(branchType);
        return connection;
    }
    private FlowSchedule mockSchedule(DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {
        FlowSchedule schedule = mock(FlowSchedule.class);
        when(schedule.getDayOfWeek()).thenReturn(dayOfWeek);
        when(schedule.getStartTime()).thenReturn(start);
        when(schedule.getEndTime()).thenReturn(end);
        return schedule;
    }


}