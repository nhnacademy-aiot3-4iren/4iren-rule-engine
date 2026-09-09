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
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradientNodeExecutorTest {

    @Mock
    private SensorTimeSeriesRepository repository;

    private GradientNodeExecutor executor;

    private static final Long ROOM_ID = 100L;
    private static final String DEV_EUI = "dev-eui-1";

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
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), eq(DEV_EUI), any(), any())).thenReturn(points);

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
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), eq(DEV_EUI), any(), any())).thenReturn(points);

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
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), eq(DEV_EUI), any(), any())).thenReturn(points);

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
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), eq(DEV_EUI), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isTrue();
        assertThat(result.path().history().getFirst().value()).isCloseTo(-0.1, within(0.0001));
    }

    @Test
    @DisplayName("알림 이력에 남기는 기울기는 소수점 둘째 자리까지만 절삭한다")
    void execute_truncatesGradientValueInNodeResult() {
        executor = new GradientNodeExecutor(repository);
        GradientNodeConfig config = gradientConfig(Operator.GT, 0.1234, 100);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(81), 20.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 30.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), eq(DEV_EUI), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isTrue();
        assertThat(result.path().history().getFirst().value()).isEqualTo(0.12);
    }

    @Test
    @DisplayName("기울기 조회는 현재 metric의 devEui와 metric updatedAt 기준 window를 사용한다")
    void execute_queriesDeviceSpecificRangeUsingMetricUpdatedAt() {
        executor = new GradientNodeExecutor(repository);
        GradientNodeConfig config = gradientConfig(Operator.GT, 0.05, 120);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant roomUpdatedAt = Instant.parse("2026-09-01T10:00:00Z");
        Instant metricUpdatedAt = Instant.parse("2026-09-01T09:59:30Z");
        FlowContext context = flowContext(roomUpdatedAt, metricUpdatedAt);

        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), eq(DEV_EUI), any(), any()))
                .thenReturn(List.of());

        executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        verify(repository).getRange(
                eq(ROOM_ID),
                eq(MeasurementType.TEMPERATURE),
                eq(DEV_EUI),
                eq(metricUpdatedAt.minusSeconds(120)),
                eq(metricUpdatedAt)
        );
    }

    @Test
    @DisplayName("현재 payload에 대상 metric이 없으면 기울기를 계산하지 않고 passed=false를 반환한다")
    void execute_failsWhenMetricMissing() {
        executor = new GradientNodeExecutor(repository);
        GradientNodeConfig config = gradientConfig(Operator.GT, 0.05, 120);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContextWithoutTemperature(now);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
        assertThat(result.path().history().getFirst().value()).isNull();
        verify(repository, org.mockito.Mockito.never()).getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), anyString(), any(), any());
    }

    private GradientNodeConfig gradientConfig(Operator operator, double gradient, int windowSec) {
        return new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.TEMPERATURE, "C/s", operator, gradient, windowSec);
    }

    private ExecutableFlow.ExecutableNode node(GradientNodeConfig config) {
        return new ExecutableFlow.ExecutableNode(1L, "gradientNode", NodeType.GRADIENT, config);
    }

    private FlowContext flowContext(Instant updatedAt) {
        return flowContext(updatedAt, updatedAt);
    }

    private FlowContext flowContext(Instant roomUpdatedAt, Instant metricUpdatedAt) {
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
        EnvironmentContext environmentContext = new EnvironmentContext(
                ROOM_ID,
                List.of(new EnvironmentContext.MetricInfo(MeasurementType.TEMPERATURE.name(), 30.0, DEV_EUI, metricUpdatedAt)),
                roomUpdatedAt
        );
        return FlowContext.of(flow, environmentContext);
    }

    private FlowContext flowContextWithoutTemperature(Instant updatedAt) {
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
        EnvironmentContext environmentContext = new EnvironmentContext(
                ROOM_ID,
                List.of(new EnvironmentContext.MetricInfo(MeasurementType.HUMIDITY.name(), 30.0, DEV_EUI, updatedAt)),
                updatedAt
        );
        return FlowContext.of(flow, environmentContext);
    }

    private FlowRuntime runtime() {
        return new FlowRuntime(new HashMap<>());
    }
}
