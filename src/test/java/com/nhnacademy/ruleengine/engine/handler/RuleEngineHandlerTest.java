package com.nhnacademy.ruleengine.engine.handler;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.engine.dispatcher.FlowDispatcher;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.flow.FlowLoader;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import com.nhnacademy.ruleengine.engine.repository.SensorTimeSeriesRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private SensorTimeSeriesRepository timeSeriesRepository;

    private RuleEngineHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RuleEngineHandler(flowLoader, dispatcher, timeSeriesRepository);
    }

    @Test
    @DisplayName("활성 flow가 없으면 시계열 데이터를 저장하지 않는다")
    void process_doesNotRecordTimeSeriesWhenNoActiveFlow() {
        EnvironmentContext context = environmentContext();
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of());

        handler.process(context).join();

        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), anyDouble(), any(Instant.class));
        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), any(String.class), anyDouble(), any(Instant.class));
    }

    @Test
    @DisplayName("시계열 조건 노드가 없으면 시계열 데이터를 저장하지 않는다")
    void process_doesNotRecordTimeSeriesForThresholdOnlyFlow() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow = flowWithNode(new ExecutableFlow.ExecutableNode(
                1L,
                "threshold",
                NodeType.THRESHOLD,
                new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0)
        ));
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of(flow));
        when(dispatcher.dispatch(List.of(flow), context)).thenReturn(CompletableFuture.completedFuture(null));

        handler.process(context).join();

        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), anyDouble(), any(Instant.class));
        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), any(String.class), anyDouble(), any(Instant.class));
    }

    @Test
    @DisplayName("AVERAGE 노드가 요구하는 metric만 room-level 시계열로 저장한다")
    void process_recordsRoomLevelTimeSeriesForAverageNode() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow = flowWithNode(new ExecutableFlow.ExecutableNode(
                1L,
                "average",
                NodeType.AVERAGE,
                new AverageNodeConfig(NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 120)
        ));
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of(flow));
        when(dispatcher.dispatch(List.of(flow), context)).thenReturn(CompletableFuture.completedFuture(null));

        handler.process(context).join();

        verify(timeSeriesRepository).save(
                eq(ROOM_ID),
                eq(MeasurementType.TEMPERATURE),
                eq(26.5),
                eq(ROOM_UPDATED_AT)
        );
        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), any(String.class), anyDouble(), any(Instant.class));
    }

    @Test
    @DisplayName("GRADIENT 노드가 요구하는 metric만 device-level 시계열로 저장한다")
    void process_recordsDeviceLevelTimeSeriesForGradientNode() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow = flowWithNode(new ExecutableFlow.ExecutableNode(
                1L,
                "gradient",
                NodeType.GRADIENT,
                new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 0.1, 90)
        ));
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of(flow));
        when(dispatcher.dispatch(List.of(flow), context)).thenReturn(CompletableFuture.completedFuture(null));

        handler.process(context).join();

        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), anyDouble(), any(Instant.class));
        verify(timeSeriesRepository).save(
                eq(ROOM_ID),
                eq(MeasurementType.TEMPERATURE),
                eq(DEV_EUI),
                eq(26.5),
                eq(METRIC_UPDATED_AT)
        );
    }

    private EnvironmentContext environmentContext() {
        return new EnvironmentContext(
                ROOM_ID,
                List.of(new EnvironmentContext.MetricInfo(MeasurementType.TEMPERATURE.name(), 26.5, DEV_EUI, METRIC_UPDATED_AT)),
                ROOM_UPDATED_AT
        );
    }

    private ExecutableFlow flowWithNode(ExecutableFlow.ExecutableNode node) {
        return ExecutableFlow.builder()
                .flowId(1L)
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
