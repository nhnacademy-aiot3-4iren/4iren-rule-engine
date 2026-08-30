package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.OrRuntimeState;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OrNodeExecutorTest {

    private final OrNodeExecutor executor = new OrNodeExecutor();

    private static final Long OR_NODE_ID = 3L;
    private static final Long TRUE_SOURCE_NODE_ID = 1L;
    private static final Long FALSE_SOURCE_NODE_ID = 2L;

    @Test
    @DisplayName("supportNodeType은 OR을 반환한다")
    void supportNodeType() {
        assertThat(executor.supportNodeType()).isEqualTo(NodeType.OR);
    }

    @Test
    @DisplayName("두 입력 경로 중 하나라도 pending상태이면 OR 조건은 판단하지 않는다 - 부조건 false반환")
    void execute_satisfiedWhenOneInputArrives() {
        ExecutableFlow flow = twoInputFlow();
        ExecutableFlow.ExecutableNode orNode = flow.nodeMap().get(OR_NODE_ID);
        FlowContext context = FlowContext.of(flow, null, Instant.now());
        FlowRuntime runtime = new FlowRuntime( new HashMap<>());

        ExecutionPath arrivedFromTrue = ExecutionPath.start(OR_NODE_ID, TRUE_SOURCE_NODE_ID, BranchType.TRUE);

        NodeExecutionResult result = executor.execute(orNode, context, arrivedFromTrue, runtime);

        assertThat(result.passed()).isFalse();
    }

    @Test
    @DisplayName("같은 OR 노드로 여러 번 도착해도 런타임 상태(OrRuntimeState)는 하나만 생성/공유된다")
    void execute_reusesSameRuntimeStateAcrossArrivals() {
        ExecutableFlow flow = twoInputFlow();
        ExecutableFlow.ExecutableNode orNode = flow.nodeMap().get(OR_NODE_ID);
        FlowContext context = FlowContext.of(flow, null, Instant.now());
        FlowRuntime runtime = new FlowRuntime( new HashMap<>());

        ExecutionPath arrivedFromTrue = ExecutionPath.start(OR_NODE_ID, TRUE_SOURCE_NODE_ID, BranchType.TRUE);
        ExecutionPath arrivedFromFalse = ExecutionPath.start(OR_NODE_ID, FALSE_SOURCE_NODE_ID, BranchType.FALSE);

        executor.execute(orNode, context, arrivedFromTrue, runtime);
        executor.execute(orNode, context, arrivedFromFalse, runtime);

        assertThat(runtime.orStateMap()).hasSize(1);
        OrRuntimeState state = runtime.orStateMap().get(OR_NODE_ID);
        assertThat(state.isReady()).isTrue(); // 두 입력 모두 ARRIVED -> PENDING 없음
    }

    @Test
    @DisplayName("실행 결과 history에 OR 노드 결과가 추가된다")
    void execute_appendsNodeResultToHistory() {
        ExecutableFlow flow = twoInputFlow();
        ExecutableFlow.ExecutableNode orNode = flow.nodeMap().get(OR_NODE_ID);
        FlowContext context = FlowContext.of(flow, null, Instant.now());
        FlowRuntime runtime = new FlowRuntime(new HashMap<>());

        ExecutionPath arrivedFromTrue = ExecutionPath.start(OR_NODE_ID, TRUE_SOURCE_NODE_ID, BranchType.TRUE);
        executor.execute(orNode, context, arrivedFromTrue, runtime);

        ExecutionPath arrivedFromFalse = ExecutionPath.start(OR_NODE_ID, FALSE_SOURCE_NODE_ID, BranchType.FALSE);
        NodeExecutionResult result = executor.execute(orNode, context, arrivedFromFalse, runtime);

        assertThat(result.passed()).isTrue();

        assertThat(result.path().history()).hasSize(1);
        assertThat(result.path().history().getFirst().nodeType()).isEqualTo(NodeType.OR.name());
    }

    private ExecutableFlow twoInputFlow() {
        ExecutableFlow.ExecutableNode orNode = new ExecutableFlow.ExecutableNode(OR_NODE_ID, "orNode", NodeType.OR, null, null);

        Map<Long, ExecutableFlow.ExecutableNode> nodeMap = new HashMap<>();
        nodeMap.put(OR_NODE_ID, orNode);

        Map<Long, List<Long>> trueAdjacencyMap = new HashMap<>();
        trueAdjacencyMap.put(TRUE_SOURCE_NODE_ID, List.of(OR_NODE_ID));

        Map<Long, List<Long>> falseAdjacencyMap = new HashMap<>();
        falseAdjacencyMap.put(FALSE_SOURCE_NODE_ID, List.of(OR_NODE_ID));

        return ExecutableFlow.builder()
                .flowId(1L)
                .flowName("flow")
                .roomId(100L)
                .schedules(List.of())
                .startNodeId(TRUE_SOURCE_NODE_ID)
                .nodeMap(nodeMap)
                .trueAdjacencyMap(trueAdjacencyMap)
                .falseAdjacencyMap(falseAdjacencyMap)
                .build();
    }
}