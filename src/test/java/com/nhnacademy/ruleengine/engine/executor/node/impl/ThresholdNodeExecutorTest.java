package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThresholdNodeExecutorTest {

    private final ThresholdNodeExecutor executor = new ThresholdNodeExecutor();

    @Test
    @DisplayName("supportNodeType은 THRESHOLD를 반환한다")
    void supportNodeType() {
        assertThat(executor.supportNodeType()).isEqualTo(NodeType.THRESHOLD);
    }

    @Test
    @DisplayName("최신 측정값이 조건을 만족하면 passed=true를 반환한다")
    void execute_passesWhenConditionMet() {
        ThresholdNodeConfig config = thresholdConfig(Operator.GT, 25.0);
        ExecutableFlow.ExecutableNode node = node(config);
        EnvironmentContext environmentContext = environmentContext("TEMPERATURE", 30.0);
        FlowContext context = flowContext(environmentContext);
        ExecutionPath path = ExecutionPath.start(node.nodeId(), null, null);

        NodeExecutionResult result = executor.execute(node, context, path, runtime());

        assertThat(result.passed()).isTrue();
        AlertEvent.NodeResult nodeResult = result.path().history().getFirst();
        assertThat(nodeResult.value()).isEqualTo(30.0);
        assertThat(nodeResult.threshold()).isEqualTo(25.0);
        assertThat(nodeResult.metricType()).isEqualTo("TEMPERATURE");
    }

    @Test
    @DisplayName("최신 측정값이 조건을 만족하지 않으면 passed=false를 반환한다")
    void execute_failsWhenConditionNotMet() {
        ThresholdNodeConfig config = thresholdConfig(Operator.GT, 25.0);
        ExecutableFlow.ExecutableNode node = node(config);
        EnvironmentContext environmentContext = environmentContext("TEMPERATURE", 20.0);
        FlowContext context = flowContext(environmentContext);
        ExecutionPath path = ExecutionPath.start(node.nodeId(), null, null);

        NodeExecutionResult result = executor.execute(node, context, path, runtime());

        assertThat(result.passed()).isFalse();
    }

    @Test
    @DisplayName("측정 지표 이름은 대소문자를 구분하지 않고 매칭된다")
    void execute_matchesMetricCaseInsensitively() {
        ThresholdNodeConfig config = thresholdConfig(Operator.GT, 25.0);
        ExecutableFlow.ExecutableNode node = node(config);
        EnvironmentContext environmentContext = environmentContext("temperature", 30.0);
        FlowContext context = flowContext(environmentContext);
        ExecutionPath path = ExecutionPath.start(node.nodeId(), null, null);

        NodeExecutionResult result = executor.execute(node, context, path, runtime());

        assertThat(result.passed()).isTrue();
    }

    @Test
    @DisplayName("해당 측정 지표의 최신값이 없으면 passed=false, value=null을 반환한다")
    void execute_failsWhenMetricMissing() {
        ThresholdNodeConfig config = thresholdConfig(Operator.GT, 25.0);
        ExecutableFlow.ExecutableNode node = node(config);
        EnvironmentContext environmentContext = environmentContext("HUMIDITY", 40.0); // TEMPERATURE 없음
        FlowContext context = flowContext(environmentContext);
        ExecutionPath path = ExecutionPath.start(node.nodeId(), null, null);

        NodeExecutionResult result = executor.execute(node, context, path, runtime());

        assertThat(result.passed()).isFalse();
        AlertEvent.NodeResult nodeResult = result.path().history().getFirst();
        assertThat(nodeResult.value()).isNull();
    }

    @Test
    @DisplayName("metrics 리스트가 null인 EnvironmentContext도 예외 없이 처리한다")
    void execute_handlesNullMetricsGracefully() {
        ThresholdNodeConfig config = thresholdConfig(Operator.GT, 25.0);
        ExecutableFlow.ExecutableNode node = node(config);
        EnvironmentContext environmentContext = new EnvironmentContext(100L, null, Instant.now());
        FlowContext context = flowContext(environmentContext);
        ExecutionPath path = ExecutionPath.start(node.nodeId(), null, null);

        NodeExecutionResult result = executor.execute(node, context, path, runtime());

        assertThat(result.passed()).isFalse();
    }

    private ThresholdNodeConfig thresholdConfig(Operator operator, double threshold) {
        return new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", operator, threshold);
    }

    private ExecutableFlow.ExecutableNode node(ThresholdNodeConfig config) {
        return new ExecutableFlow.ExecutableNode(1L, "thresholdNode", NodeType.THRESHOLD, config, null);
    }

    private EnvironmentContext environmentContext(String metric, double value) {
        return new EnvironmentContext(
                100L,
                List.of(new EnvironmentContext.MetricInfo(metric, value, "dev-eui-1", Instant.now())),
                Instant.now()
        );
    }

    private FlowContext flowContext(EnvironmentContext environmentContext) {
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
        return FlowContext.of(flow, environmentContext, Instant.now());
    }

    private FlowRuntime runtime() {
        return new FlowRuntime(new HashMap<>());
    }
}