package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradientNodeExecutorTest {

    @Mock
    private SensorTimeSeriesRepository repository;

    private GradientNodeExecutor executor;

    private static final Long ROOM_ID = 100L;

    @Test
    @DisplayName("supportNodeType은 GRADIENT를 반환한다")
    void supportNodeType() {
        executor = new GradientNodeExecutor(repository);
        assertThat(executor.supportNodeType()).isEqualTo(NodeType.GRADIENT);
    }

    @Test
    @DisplayName("가장 오래된 값과 최신 값으로 초당 변화율을 계산한다")
    void execute_calculatesGradientFromOldestAndLatestPoint() {
        executor = new GradientNodeExecutor(repository);
        GradientNodeConfig config = gradientConfig(Operator.GT, 0.05, 100);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        // 100초 동안 20.0 -> 30.0으로 상승 => 기울기 0.1/s (중간 포인트는 계산에 영향 없음: 가장 오래된/최신 값만 사용)
        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(100), 20.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(50), 25.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 30.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isTrue();
        AlertEvent.NodeResult nodeResult = result.path().history().getFirst();
        assertThat(nodeResult.value()).isCloseTo(0.1, within(0.0001));
    }

    @Test
    @DisplayName("데이터가 2개 미만이면 기울기를 계산하지 않고 passed=false를 반환한다")
    void execute_failsWhenFewerThanTwoPoints() {
        executor = new GradientNodeExecutor(repository);
        GradientNodeConfig config = gradientConfig(Operator.GT, 0.0, 100);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 20.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
        assertThat(result.path().history().getFirst().value()).isNull();
    }

    @Test
    @DisplayName("가장 오래된 값과 최신 값의 시각이 동일하면 0으로 나누지 않고 passed=false를 반환한다")
    void execute_failsWhenElapsedTimeIsZero() {
        executor = new GradientNodeExecutor(repository);
        GradientNodeConfig config = gradientConfig(Operator.GT, 0.0, 100);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 20.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 25.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
        assertThat(result.path().history().getFirst().value()).isNull();
    }

    @Test
    @DisplayName("값이 하락하면 기울기는 음수로 계산된다")
    void execute_negativeGradientWhenValueDecreases() {
        executor = new GradientNodeExecutor(repository);
        GradientNodeConfig config = gradientConfig(Operator.LT, -0.05, 100);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(100), 30.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 20.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isTrue();
        assertThat(result.path().history().getFirst().value()).isCloseTo(-0.1, within(0.0001));
    }

    private GradientNodeConfig gradientConfig(Operator operator, double gradient, int windowSec) {
        return new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.TEMPERATURE, "C/s", operator, gradient, windowSec);
    }

    private ExecutableFlow.ExecutableNode node(GradientNodeConfig config) {
        return new ExecutableFlow.ExecutableNode(1L, "gradientNode", NodeType.GRADIENT, config);
    }

    private FlowContext flowContext(Instant triggeredAt) {
        ExecutableFlow flow = ExecutableFlow.builder()
                .flowId(1L)
                .flowName("flow")
                .roomId(ROOM_ID)
                .schedules(List.of())
                .startNodeId(1L)
                .nodeMap(new HashMap<>())
                .trueAdjacencyMap(new HashMap<>())
                .falseAdjacencyMap(new HashMap<>())
                .build();
        return FlowContext.of(flow, null, triggeredAt);
    }

    private FlowRuntime runtime() {
        return new FlowRuntime(new HashMap<>());
    }
}