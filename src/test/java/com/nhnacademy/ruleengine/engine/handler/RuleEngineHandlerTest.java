package com.nhnacademy.ruleengine.engine.handler;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.engine.dispatcher.FlowDispatcher;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.flow.FlowLoader;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineHandlerTest {

    private static final Long ROOM_ID = 100L;
    private static final String DEV_EUI = "dev-eui-1";
    private static final Instant ROOM_UPDATED_AT = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant METRIC_UPDATED_AT = Instant.parse("2026-09-01T09:59:30Z");

    @Mock
    private FlowLoader flowLoader;

    @Mock
    private FlowDispatcher dispatcher;

    @Mock
    private TimeSeriesPreparationService timeSeriesPreparationService;

    private RuleEngineHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RuleEngineHandler(flowLoader, timeSeriesPreparationService, dispatcher);
    }

    @Test
    @DisplayName("활성 flow가 없으면 시계열 준비와 dispatch를 하지 않는다")
    void process_doesNotPrepareTimeSeriesAndDispatchWhenNoActiveFlow() {
        EnvironmentContext context = environmentContext();
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of());

        handler.process(context).join();

        verify(timeSeriesPreparationService, never()).prepareAndFilterDispatchableFlows(context, List.of());
        verify(dispatcher, never()).dispatch(List.of(), context);
    }

    @Test
    @DisplayName("시계열 준비 후 실행 가능한 flow만 dispatch 한다")
    void process_dispatchesOnlyPreparedFlows() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow1 = flowWithIdAndNode(1L, new ExecutableFlow.ExecutableNode(
                1L,
                "threshold",
                NodeType.THRESHOLD,
                new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0)
        ));
        ExecutableFlow flow2 = flowWithIdAndNode(2L, new ExecutableFlow.ExecutableNode(
                2L,
                "threshold",
                NodeType.THRESHOLD,
                new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.LT, 30.0)
        ));
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of(flow1, flow2));
        when(timeSeriesPreparationService.prepareAndFilterDispatchableFlows(context, List.of(flow1, flow2))).thenReturn(List.of(flow2));
        when(dispatcher.dispatch(List.of(flow2), context)).thenReturn(CompletableFuture.completedFuture(null));

        handler.process(context).join();

        verify(timeSeriesPreparationService).prepareAndFilterDispatchableFlows(context, List.of(flow1, flow2));
        verify(dispatcher).dispatch(List.of(flow2), context);
    }

    @Test
    @DisplayName("시계열 준비 후 실행 가능한 flow가 없으면 dispatch 하지 않는다")
    void process_doesNotDispatchWhenNoPreparedFlow() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow = flowWithNode(new ExecutableFlow.ExecutableNode(
                1L,
                "threshold",
                NodeType.THRESHOLD,
                new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0)
        ));
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of(flow));
        when(timeSeriesPreparationService.prepareAndFilterDispatchableFlows(context, List.of(flow))).thenReturn(List.of());

        handler.process(context).join();

        verify(dispatcher, never()).dispatch(List.of(), context);
    }

    private EnvironmentContext environmentContext() {
        return new EnvironmentContext(
                ROOM_ID,
                List.of(new EnvironmentContext.MetricInfo(MeasurementType.TEMPERATURE.name(), 26.5, DEV_EUI, METRIC_UPDATED_AT)),
                ROOM_UPDATED_AT
        );
    }

    private ExecutableFlow flowWithNode(ExecutableFlow.ExecutableNode node) {
        return flowWithIdAndNode(1L, node);
    }

    private ExecutableFlow flowWithIdAndNode(Long flowId, ExecutableFlow.ExecutableNode node) {
        return ExecutableFlow.builder()
                .flowId(flowId)
                .flowName("flow")
                .roomId(ROOM_ID)
                .schedules(List.of())
                .startNodeId(0L)
                .nodeMap(Map.of(0L, new ExecutableFlow.ExecutableNode(0L, "start", NodeType.START, null), node.nodeId(), node))
                .trueAdjacencyMap(Map.of())
                .falseAdjacencyMap(Map.of())
                .build();
    }
}
