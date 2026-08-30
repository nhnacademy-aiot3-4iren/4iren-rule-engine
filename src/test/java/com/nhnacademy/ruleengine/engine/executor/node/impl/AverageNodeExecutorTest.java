package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AverageNodeExecutorTest {

    @Mock
    private SensorTimeSeriesRepository repository;

    private AverageNodeExecutor executor;

    private static final Long ROOM_ID = 100L;

    @Test
    @DisplayName("supportNodeType은 AVERAGE를 반환한다")
    void supportNodeType() {
        executor = new AverageNodeExecutor(repository);
        assertThat(executor.supportNodeType()).isEqualTo(NodeType.AVERAGE);
    }

    @Test
    @DisplayName("윈도우 내 평균값이 조건을 만족하면 passed=true를 반환한다")
    void execute_passesWhenAverageMeetsCondition() {
        executor = new AverageNodeExecutor(repository);
        AverageNodeConfig config = averageConfig(Operator.GT, 25.0, 60);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(30), 26.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(10), 30.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isTrue();
        AlertEvent.NodeResult nodeResult = result.path().history().getFirst();
        assertThat(nodeResult.value()).isCloseTo(28.0, within(0.0001));
    }

    @Test
    @DisplayName("윈도우 내 평균값이 조건을 만족하지 않으면 passed=false를 반환한다")
    void execute_failsWhenAverageDoesNotMeetCondition() {
        executor = new AverageNodeExecutor(repository);
        AverageNodeConfig config = averageConfig(Operator.GT, 25.0, 60);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(30), 10.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(10), 12.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
    }

    @Test
    @DisplayName("윈도우 내 데이터가 없으면 passed=false, value=null을 반환한다")
    void execute_failsWhenNoDataInWindow() {
        executor = new AverageNodeExecutor(repository);
        AverageNodeConfig config = averageConfig(Operator.GT, 25.0, 60);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(List.of());

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
        assertThat(result.path().history().getFirst().value()).isNull();
    }

    @Test
    @DisplayName("windowSec만큼의 과거 시점부터 현재까지의 범위로 조회한다")
    void execute_queriesCorrectTimeRange() {
        executor = new AverageNodeExecutor(repository);
        AverageNodeConfig config = averageConfig(Operator.GT, 25.0, 120);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(List.of());

        executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), fromCaptor.capture(), toCaptor.capture());

        assertThat(toCaptor.getValue()).isEqualTo(now);
        assertThat(fromCaptor.getValue()).isEqualTo(now.minusSeconds(120));
    }

    private AverageNodeConfig averageConfig(Operator operator, double average, int windowSec) {
        return new AverageNodeConfig(NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", operator, average, windowSec);
    }

    private ExecutableFlow.ExecutableNode node(AverageNodeConfig config) {
        return new ExecutableFlow.ExecutableNode(1L, "averageNode", NodeType.AVERAGE, config, null);
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
        return new FlowRuntime( new HashMap<>());
    }
}