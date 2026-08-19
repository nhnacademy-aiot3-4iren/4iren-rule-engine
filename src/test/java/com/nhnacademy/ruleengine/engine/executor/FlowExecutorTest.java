package com.nhnacademy.ruleengine.engine.executor;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutorRegistry;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.OrRuntimeState;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowExecutorTest {

    @Mock
    private NodeExecutorRegistry nodeExecutorRegistry;

    @Mock
    private OrRuntimeState orRuntimeState;

    private FlowExecutor flowExecutor;

    ExecutableFlow.ExecutableSchedule schedule1;

    ExecutableFlow.ExecutableNode node0;
    ExecutableFlow.ExecutableNode node1;
    ExecutableFlow.ExecutableNode node2;
    ExecutableFlow.ExecutableNode node3;
    ExecutableFlow.ExecutableNode node4;
    @BeforeEach
    void setUp() {
        flowExecutor = new FlowExecutor(nodeExecutorRegistry);
        schedule1 = new ExecutableFlow.ExecutableSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));


        node0 = new ExecutableFlow.ExecutableNode(0L, "시작노드", NodeType.START, null, 300);
        node1 = new ExecutableFlow.ExecutableNode(1L, "노드1", NodeType.THRESHOLD, null, 300);
        node2 = new ExecutableFlow.ExecutableNode(2L, "노드2", NodeType.AVERAGE, null, 300);
        node3 = new ExecutableFlow.ExecutableNode(3L, "노드3", NodeType.OR, null, 300);
        node4 = new ExecutableFlow.ExecutableNode(4L, "노드5", NodeType.GRADIENT, null, 300);

    }


    private ExecutableFlow createFlow(){
        return new ExecutableFlow(1L, "플로우1", 1L,
                List.of(schedule1),
                0L,
                Map.of(
                        0L, node0,
                        1L, node1,
                        2L, node2,
                        3L, node3,

                        4L, node4),
                Map.of(
                        0L,List.of(1L, 2L),

                        2L, List.of(4L),
                        4L, List.of(3L)
                ),
                Map.of(
                        1L, List.of(3L)
                )
        );
    }

    @Test
    @DisplayName("start노드에서 연결된 1번과 2번 노드가 모두 실행된다")
    void executes_all_start_true_branch_nodes() {
        // given
        ExecutableFlow flow = createFlow();

        FlowContext context = mock(FlowContext.class);
        when(nodeExecutorRegistry.execute(eq(NodeType.THRESHOLD), eq(node1),eq(context), any(), any())).thenReturn(true);
        when(nodeExecutorRegistry.execute(eq(NodeType.AVERAGE),eq(node2),eq(context), any(), any())).thenReturn(false);

        flowExecutor.execute(flow, context);

        verify(nodeExecutorRegistry, times(1))
                .execute(eq(NodeType.THRESHOLD), eq(node1), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(NodeType.AVERAGE), eq(node2), eq(context), any(), any());

    }

    /*
    0(start) -(true)-> 1(condition) -(false)-> 3(or)
         -(true)-> 2(condition) -(false)-> end
                                 -(true)--> 4(conditions) -(true)-> 3(or)
    */
    @Test
    @DisplayName("경로별 진행상황이 or노드에 도달함에 따라 or노드가 두번 실행된다")
    void or_node_should_execute_twice_as_each_path_arrives() {
        ExecutableFlow flow = createFlow();
        FlowContext context = mock(FlowContext.class);


        when(nodeExecutorRegistry.execute(eq(NodeType.THRESHOLD),eq(node1), eq(context), any(), any())).thenReturn(false);
        when(nodeExecutorRegistry.execute(eq(NodeType.AVERAGE),eq(node2),eq(context), any(), any())).thenReturn(true);
        when(nodeExecutorRegistry.execute(eq(NodeType.OR),eq(node3), eq(context), any(), any())).thenReturn(true);
        when(nodeExecutorRegistry.execute(eq(NodeType.GRADIENT),eq(node4), eq(context), any(), any())).thenReturn(true);



        flowExecutor.execute(flow, context);

        verify(nodeExecutorRegistry,times(1)).execute(eq(NodeType.THRESHOLD), eq(node1), eq(context), any(), any());
        verify(nodeExecutorRegistry,times(1)).execute(eq(NodeType.AVERAGE), eq(node2), eq(context), any(), any());
        verify(nodeExecutorRegistry,times(2)).execute(eq(NodeType.OR), eq(node3), eq(context), any(), any());
        verify(nodeExecutorRegistry,times(1)).execute(eq(NodeType.GRADIENT), eq(node4), eq(context), any(), any());

    }


    //


}