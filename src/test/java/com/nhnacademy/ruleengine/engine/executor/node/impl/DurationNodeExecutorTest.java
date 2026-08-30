package com.nhnacademy.ruleengine.engine.executor.node.impl;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.ruleengine.engine.executor.ExecutionPath;
import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutionResult;
import com.nhnacademy.ruleengine.engine.executor.runtimestate.FlowRuntime;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DurationNodeExecutorTest {

    @Mock
    private SensorTimeSeriesRepository repository;

    private DurationNodeExecutor executor;

    private static final Long ROOM_ID = 100L;

    @Test
    @DisplayName("supportNodeType은 DURATION을 반환한다")
    void supportNodeType() {
        executor = new DurationNodeExecutor(repository);
        assertThat(executor.supportNodeType()).isEqualTo(NodeType.DURATION);
    }

    @Test
    @DisplayName("윈도우 전 구간이 조건을 만족하고 데이터가 충분히 쌓였으면 passed=true를 반환한다")
    void execute_passesWhenAllPointsSatisfyConditionAndDataIsSufficient() {
        executor = new DurationNodeExecutor(repository);
        int durationSec = 60;
        DurationNodeConfig config = durationConfig(Operator.GT, 25.0, durationSec);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        // 가장 오래된 포인트가 정확히 (now - durationSec) 시점 => 데이터 충분성 조건 만족(isAfter가 아님)
        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(durationSec), 26.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(30), 27.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 28.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isTrue();
        assertThat(result.path().history().getFirst().value()).isEqualTo(28.0); // 마지막(최신) 값
    }

    @Test
    @DisplayName("중간에 조건을 벗어난 포인트가 하나라도 있으면 passed=false를 반환한다")
    void execute_failsWhenAnyPointViolatesCondition() {
        executor = new DurationNodeExecutor(repository);
        int durationSec = 60;
        DurationNodeConfig config = durationConfig(Operator.GT, 25.0, durationSec);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(durationSec), 26.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(30), 20.0), // 조건 위반
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 28.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
    }

    @Test
    @DisplayName("가장 오래된 데이터가 durationSec만큼 쌓이지 않았으면 passed=false를 반환한다")
    void execute_failsWhenNotEnoughHistoryYet() {
        executor = new DurationNodeExecutor(repository);
        int durationSec = 60;
        DurationNodeConfig config = durationConfig(Operator.GT, 25.0, durationSec);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        // 가장 오래된 포인트가 (now - durationSec)보다 나중 시점 => 데이터 충분성 조건 위반
        List<SensorTimeSeriesRepository.TimeSeriesPoint> points = List.of(
                new SensorTimeSeriesRepository.TimeSeriesPoint(now.minusSeconds(10), 26.0),
                new SensorTimeSeriesRepository.TimeSeriesPoint(now, 28.0)
        );
        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(points);

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
    }

    @Test
    @DisplayName("윈도우 내 데이터가 없으면 passed=false, value=null을 반환한다")
    void execute_failsWhenNoData() {
        executor = new DurationNodeExecutor(repository);
        DurationNodeConfig config = durationConfig(Operator.GT, 25.0, 60);
        ExecutableFlow.ExecutableNode node = node(config);
        Instant now = Instant.now();
        FlowContext context = flowContext(now);

        when(repository.getRange(eq(ROOM_ID), eq(MeasurementType.TEMPERATURE), any(), any())).thenReturn(List.of());

        NodeExecutionResult result = executor.execute(node, context, ExecutionPath.start(node.nodeId(), null, null), runtime());

        assertThat(result.passed()).isFalse();
        assertThat(result.path().history().getFirst().value()).isNull();
    }

    private DurationNodeConfig durationConfig(Operator operator, double threshold, int durationSec) {
        return new DurationNodeConfig(NodeType.DURATION, 0, 0, MeasurementType.TEMPERATURE, "C", operator, threshold, durationSec);
    }

    private ExecutableFlow.ExecutableNode node(DurationNodeConfig config) {
        return new ExecutableFlow.ExecutableNode(1L, "durationNode", NodeType.DURATION, config, null);
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