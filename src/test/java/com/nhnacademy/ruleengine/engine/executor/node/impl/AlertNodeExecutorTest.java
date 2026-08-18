package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertChannel;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlertNodeExecutorTest {

    private final AlertNodeExecutor executor = new AlertNodeExecutor();

    @Test
    @DisplayName("supportNodeType은 ALERT를 반환한다")
    void supportNodeType() {
        assertThat(executor.supportNodeType()).isEqualTo(NodeType.ALERT);
    }

    @Test
    @DisplayName("실행 시 항상 passed=true를 반환하고 전달받은 path를 그대로 유지한다")
    void execute_alwaysPassesAndKeepsPathUnchanged() {
        AlertNodeConfig config = new AlertNodeConfig(
                NodeType.ALERT, 0, 0, AlertChannel.TELEGRAM, "CO2 농도 경고", AlertType.VENTILATION_RECOMMEND
        );
        ExecutableFlow.ExecutableNode node = new ExecutableFlow.ExecutableNode(1L, "alertNode", NodeType.ALERT, config, null);
        FlowContext context = flowContext();
        ExecutionPath path = ExecutionPath.start(node.nodeId(), null, null);

        NodeExecutionResult result = executor.execute(node, context, path, runtime());

        assertThat(result.passed()).isTrue();
        assertThat(result.path()).isSameAs(path);
    }

    private FlowContext flowContext() {
        ExecutableFlow flow = ExecutableFlow.builder()
                .flowId(1L)
                .flowName("flow")
                .roomId(100L)
                .schedules(List.of())
                .startNodeId(1L)
                .nodeMap(new HashMap<>())
                .trueAdjacencyMap(new HashMap<>())
                .falseAdjacencyMap(new HashMap<>())
                .build();
        return FlowContext.of(flow, null, Instant.now());
    }

    private FlowRuntime runtime() {
        return new FlowRuntime(new LinkedList<>(), new HashMap<>());
    }
}