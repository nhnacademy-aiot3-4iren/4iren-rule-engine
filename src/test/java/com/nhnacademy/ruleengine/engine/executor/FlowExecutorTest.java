package com.nhnacademy.ruleengine.engine.executor;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutorRegistry;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.LogicalInputKey;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.OrRuntimeState;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    private FlowExecutor flowExecutor;

    ExecutableFlow.ExecutableSchedule schedule;

    ExecutableFlow.ExecutableNode node0;
    ExecutableFlow.ExecutableNode node1;
    ExecutableFlow.ExecutableNode node2;
    ExecutableFlow.ExecutableNode node3;
    ExecutableFlow.ExecutableNode node4;
    ExecutableFlow.ExecutableNode node5;
    ExecutableFlow.ExecutableNode node6;
    ExecutableFlow.ExecutableNode node7;

    @BeforeEach
    void setUp() {
        flowExecutor = new FlowExecutor(nodeExecutorRegistry);
        schedule = new ExecutableFlow.ExecutableSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));


        node0 = new ExecutableFlow.ExecutableNode(0L, "시작노드", NodeType.START, null, 300);
        node1 = new ExecutableFlow.ExecutableNode(1L, "노드1", NodeType.THRESHOLD, null, 300);
        node2 = new ExecutableFlow.ExecutableNode(2L, "노드2", NodeType.AVERAGE, null, 300);
        node3 = new ExecutableFlow.ExecutableNode(3L, "노드3", NodeType.OR, null, 300);
        node4 = new ExecutableFlow.ExecutableNode(4L, "노드4", NodeType.GRADIENT, null, 300);
        node5 = new ExecutableFlow.ExecutableNode(5L, "노드5", NodeType.THRESHOLD, null, 300);
        node6 = new ExecutableFlow.ExecutableNode(6L, "노드6", NodeType.OR, null, 300);
        node7 = new ExecutableFlow.ExecutableNode(7L, "노드7", NodeType.DURATION, null, 300);

    }

    /*
    0(start) -true-> 1(condition) -false-> 3(or) -true-> 7(condition) -true-> 6(or)
             -true-> 2(condition) -true-> 5(condition) -true-> 6(or)
                                  -true-> 4(conditions) -true-> 3(or)
                                  -false->
    */


    private ExecutableFlow createFlow(){
        return new ExecutableFlow(1L, "플로우1", 1L,
                List.of(schedule),
                0L,
                Map.of(
                        0L, node0,
                        1L, node1,
                        2L, node2,
                        3L, node3,
                        4L, node4,
                        5L, node5,
                        6L, node6,
                        7L, node7
                ),
                Map.of(
                        0L,List.of(1L, 2L),

                        2L, List.of(4L, 5L),
                        3L, List.of(7L),

                        5L, List.of(6L),
                        7L, List.of(6L)

                ),
                Map.of(
                        1L, List.of(3L),
                        4L, List.of(3L)
                )
        );
    }

    private void stubPass(ExecutableFlow.ExecutableNode node, FlowContext context){
        when(nodeExecutorRegistry.execute(
                eq(node.nodeType()),
                eq(node),
                eq(context),
                any(ExecutionPath.class),
                any(FlowRuntime.class)
        )).thenAnswer(invocationOnMock -> {
            ExecutionPath beforePath = invocationOnMock.getArgument(3);
            ExecutionPath appendedPath = beforePath.append(createNodeResult(node));
            return NodeExecutionResult.of(true, appendedPath);
        });
    }

    private void stubFail(ExecutableFlow.ExecutableNode node, FlowContext  context){
        when(nodeExecutorRegistry.execute(
                eq(node.nodeType()),
                eq(node),
                eq(context),
                any(ExecutionPath.class),
                any(FlowRuntime.class)
        ))       .thenAnswer(invocationOnMock -> {
            ExecutionPath beforePath = invocationOnMock.getArgument(3);
            ExecutionPath appendedPath = beforePath.append(createNodeResult(node));
            return NodeExecutionResult.of(false, appendedPath);
        });
    }

    private void stubOr(ExecutableFlow.ExecutableNode node, FlowContext  context){
        when(nodeExecutorRegistry.execute(
                eq(node.nodeType()),
                eq(node),
                eq(context),
                any(ExecutionPath.class),
                any(FlowRuntime.class)
        )).thenAnswer(invocationOnMock -> {
            ExecutionPath beforePath = invocationOnMock.getArgument(3);
            FlowRuntime runtime = invocationOnMock.getArgument(4);

            OrRuntimeState state = runtime.orStateMap().get(node.nodeId());
            state.markArrived(
                    new LogicalInputKey(beforePath.fromNodeId(), beforePath.fromBranchType(), node.nodeId()),
                    beforePath.history()
            );

            if(!state.isReady()){
                return NodeExecutionResult.of(false, beforePath);
            }

            List<AlertEvent.NodeResult> mergedPath = state.mergeArrivedHistories();
            mergedPath.add(createNodeResult(node));

            return NodeExecutionResult.of(state.isSatisfied(), beforePath.appendMergedResult(mergedPath));
        });
    }

    private FlowContext createContext(ExecutableFlow flow) {
        FlowContext context = mock(FlowContext.class);
        when(context.flow()).thenReturn(flow);
        return context;
    }

    public AlertEvent.NodeResult createNodeResult(
            ExecutableFlow.ExecutableNode node
    ){
        NodeConfig nodeConfig = node.nodeConfig();

        if(node.nodeType() == NodeType.OR){
            return new AlertEvent.NodeResult(
                    node.nodeType().toString(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        return new AlertEvent.NodeResult(
                node.nodeType().toString(),
                nodeConfig != null && nodeConfig.measurementType() != null
                        ? nodeConfig.measurementType().toString()
                        : null,
                "operator",
                "unit",
                1.0,
                2.0
        );
    }

    @Test
    @DisplayName("시작 노드의 true 분기들이 초기 실행 큐에 들어가고 순서대로 실행")
    void executes_all_after_startNode() {

        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubPass(node1,context);
        stubFail(node2, context);

        flowExecutor.execute(context);

        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node1.nodeType()), eq(node1), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node2.nodeType()), eq(node2), eq(context), any(), any());
    }


    @Test
    @DisplayName("OR노드 실행 안됨 - 모든 경로 blocked")
    void or_node_should_not_execute_when_all_paths_are_blocked() {
        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubPass(node1,context);
        stubFail(node2, context);

        flowExecutor.execute(context);

        verify(nodeExecutorRegistry, never())
                .execute(eq(node3.nodeType()), eq(node3), eq(context), any(), any());

        verify(nodeExecutorRegistry, never())
                .execute(eq(node5.nodeType()), eq(node5), eq(context), any(), any());
    }

    @Test
    @DisplayName("OR노드 정상 실행 - 모든 경로 arrived")
    void or_node_should_execute_twice_as_each_path_arrives() {
        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubFail(node1,context);
        stubPass(node2, context);
        stubFail(node5, context);
        stubFail(node4, context);
        stubOr(node3, context);
        stubFail(node7, context);

        flowExecutor.execute(context);

        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node1.nodeType()), eq(node1), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node2.nodeType()), eq(node2), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node5.nodeType()), eq(node5), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node4.nodeType()), eq(node4), eq(context), any(), any());

        //모든 경로가 arrived이므로 or노드 2번 실행(경로의 개수만큼)
        verify(nodeExecutorRegistry, times(2))
                .execute(eq(node3.nodeType()), eq(node3), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node7.nodeType()), eq(node7), eq(context), any(), any());
        verify(nodeExecutorRegistry, never())
                .execute(eq(node6.nodeType()), eq(node6), eq(context), any(), any());
    }

    @Test
    @DisplayName("OR노드 정상 실행 - 마지막 경로 arrived")
    void or_node_should_execute_when_last_path_arrives() {
        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubPass(node1, context);//blocked
        stubPass(node2, context);
        stubFail(node5, context);
        stubFail(node4, context);
        stubOr(node3, context);
        stubFail(node7, context);

        flowExecutor.execute(context);

        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node1.nodeType()), eq(node1), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node2.nodeType()), eq(node2), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node4.nodeType()), eq(node4), eq(context), any(), any());

        //경로 1개만 arrived이므로 1회 실행
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node3.nodeType()), eq(node3), eq(context), any(), any());
    }

    @Test
    @DisplayName("OR노드 reevaluate - 마지막 경로가 blocked")
    void or_node_should_reevaluate_when_last_path_is_blocked() {
        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubFail(node1, context);
        stubFail(node2, context);
        stubOr(node3, context);

        stubFail(node7, context);

        flowExecutor.execute(context);

        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node1.nodeType()), eq(node1), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node2.nodeType()), eq(node2), eq(context), any(), any());

        //경로 1개만 arrived이므로 1회 실행
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node3.nodeType()), eq(node3), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node7.nodeType()), eq(node7), eq(context), any(), any());

    }

    @Test
    @DisplayName("blocked 전파 - or노드 뒤의 or노드에도 전파되어야한다.")
    void blocked_should_propagate_to_next_or_node (){
        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubPass(node1, context);
        stubPass(node2, context);
        stubPass(node4, context);
        stubPass(node5, context);
        stubOr(node6, context);

        flowExecutor.execute(context);

        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node1.nodeType()), eq(node1), eq(context), any(), any());
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node2.nodeType()), eq(node2), eq(context), any(), any());

        // node1 -> node3
        // node2 -> node4 -> node3
        // 두 경로 모두 blocked되어 node3은 실행되지 않음
        verify(nodeExecutorRegistry, never())
                .execute(eq(node3.nodeType()), eq(node3), eq(context), any(), any());

        //blocked 경로이므로 실행되지 않음
        verify(nodeExecutorRegistry, never())
                .execute(eq(node7.nodeType()), eq(node7), eq(context), any(), any());

        // node1 -> node3 -> node6 : blocked
        // node2 -> node5 -> node6 : arrived
        // 따라서 node6은 실행됨
        verify(nodeExecutorRegistry, times(1))
                .execute(eq(node6.nodeType()), eq(node6), eq(context), any(), any());
    }


    @Test
    @DisplayName("ExecutionPath history 누적")
    void execution_path_history_should_accumulate() {
        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubPass(node1, context);
        stubPass(node2, context);
        stubFail(node4, context);
        stubOr(node3, context);
        stubFail(node5, context);
        stubFail(node7, context);

        flowExecutor.execute(context);

        //node2(AVERAGE) -> node4 (GRADIENT)->  node3(OR, path history 누적
        verify(nodeExecutorRegistry).execute(
                eq(node3.nodeType()),
                eq(node3),
                eq(context),
                argThat(path -> {
                    List<String> nodeTypes = path.history().stream()
                            .map(AlertEvent.NodeResult::nodeType)
                            .toList();

                    return nodeTypes.contains("AVERAGE")
                            && nodeTypes.contains("GRADIENT");
                }),
                any(FlowRuntime.class)
        );
    }


    @Test
    @DisplayName("ExecutionPath history 누적 - OR노드 history 병합")
    void execution_path_history_should_be_merged_at_or_node() {
        ExecutableFlow flow = createFlow();
        FlowContext context = createContext(flow);

        stubFail(node1, context);
        stubPass(node2, context);
        stubFail(node4, context);
        stubOr(node3, context);
        stubFail(node5, context);
        stubFail(node7, context);

        flowExecutor.execute(context);


        //node2(AVERAGE) -> node4 (GRADIENT)->  node3(OR), path history 누적
        //node1(THRESHOLD) -> node3(OR), path history 누적
        //node3(OR) -> node7(DURATION) path history 병합 후 node7 execute로 입력
        verify(nodeExecutorRegistry).execute(
                eq(node7.nodeType()),
                eq(node7),
                eq(context),
                argThat(path -> {
                    List<String> nodeTypes = path.history().stream()
                            .map(AlertEvent.NodeResult::nodeType)
                            .toList();

                    return nodeTypes.contains("AVERAGE")
                            && nodeTypes.contains("GRADIENT")
                            && nodeTypes.contains("THRESHOLD")
                            && nodeTypes.contains("OR");
                }),
                any(FlowRuntime.class)
        );
    }

}