package com.nhnacademy.ruleengine.engine.handler;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import com.nhnacademy.ruleengine.engine.model.FlowFailureEvent;
import com.nhnacademy.ruleengine.engine.publisher.FlowFailureEventPublisher;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeSeriesPreparationServiceTest {

    private static final Long ROOM_ID = 100L;
    private static final String DEV_EUI = "dev-eui-1";
    private static final Instant ROOM_UPDATED_AT = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant METRIC_UPDATED_AT = Instant.parse("2026-09-01T09:59:30Z");

    @Mock
    private SensorTimeSeriesRepository timeSeriesRepository;

    @Mock
    private FlowFailureEventPublisher flowFailureEventPublisher;

    private TimeSeriesPreparationService service;

    @BeforeEach
    void setUp() {
        service = new TimeSeriesPreparationService(timeSeriesRepository, flowFailureEventPublisher);
    }

    @Test
    @DisplayName("시계열 조건 노드가 없으면 저장 없이 dispatch 대상에 포함한다")
    void prepare_includesThresholdOnlyFlowWithoutRecordingTimeSeries() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow = flowWithNode(new ExecutableFlow.ExecutableNode(
                1L,
                "threshold",
                NodeType.THRESHOLD,
                new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0)
        ));

        List<ExecutableFlow> result = service.prepareAndFilterDispatchableFlows(context, List.of(flow));

        assertThat(result).containsExactly(flow);
        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), anyDouble(), any(Instant.class));
        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), any(String.class), anyDouble(), any(Instant.class));
    }

    @Test
    @DisplayName("AVERAGE 노드가 요구하는 metric만 room-level 시계열로 저장한다")
    void prepare_recordsRoomLevelTimeSeriesForAverageNode() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow = flowWithNode(new ExecutableFlow.ExecutableNode(
                1L,
                "average",
                NodeType.AVERAGE,
                new AverageNodeConfig(NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 120)
        ));

        List<ExecutableFlow> result = service.prepareAndFilterDispatchableFlows(context, List.of(flow));

        assertThat(result).containsExactly(flow);
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
    void prepare_recordsDeviceLevelTimeSeriesForGradientNode() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow = flowWithNode(new ExecutableFlow.ExecutableNode(
                1L,
                "gradient",
                NodeType.GRADIENT,
                new GradientNodeConfig(NodeType.GRADIENT, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 0.1, 90)
        ));

        List<ExecutableFlow> result = service.prepareAndFilterDispatchableFlows(context, List.of(flow));

        assertThat(result).containsExactly(flow);
        verify(timeSeriesRepository, never()).save(any(Long.class), any(MeasurementType.class), anyDouble(), any(Instant.class));
        verify(timeSeriesRepository).save(
                eq(ROOM_ID),
                eq(MeasurementType.TEMPERATURE),
                eq(DEV_EUI),
                eq(26.5),
                eq(METRIC_UPDATED_AT)
        );
    }

    @Test
    @DisplayName("시계열 저장 실패 flow는 실패 이벤트를 발행하고 dispatch 대상에서 제외한다")
    void prepare_excludesFlowWhenTimeSeriesSaveFails() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow averageFlow = flowWithIdAndNode(1L, new ExecutableFlow.ExecutableNode(
                1L,
                "average",
                NodeType.AVERAGE,
                new AverageNodeConfig(NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 120)
        ));
        ExecutableFlow thresholdFlow = flowWithIdAndNode(2L, new ExecutableFlow.ExecutableNode(
                2L,
                "threshold",
                NodeType.THRESHOLD,
                new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0)
        ));
        RuntimeException redisFailure = new RuntimeException("redis down");

        doThrow(redisFailure)
                .when(timeSeriesRepository)
                .save(ROOM_ID, MeasurementType.TEMPERATURE, 26.5, ROOM_UPDATED_AT);

        List<ExecutableFlow> result = service.prepareAndFilterDispatchableFlows(context, List.of(averageFlow, thresholdFlow));

        assertThat(result).containsExactly(thresholdFlow);
        verify(flowFailureEventPublisher).publish(any(FlowFailureEvent.class));
    }

    @Test
    @DisplayName("여러 flow가 같은 시계열 저장을 요구하면 한 번만 저장한다")
    void prepare_recordsSharedTimeSeriesOnlyOnce() {
        EnvironmentContext context = environmentContext();
        ExecutableFlow flow1 = flowWithIdAndNode(1L, new ExecutableFlow.ExecutableNode(
                1L,
                "average-1",
                NodeType.AVERAGE,
                new AverageNodeConfig(NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 25.0, 120)
        ));
        ExecutableFlow flow2 = flowWithIdAndNode(2L, new ExecutableFlow.ExecutableNode(
                2L,
                "average-2",
                NodeType.AVERAGE,
                new AverageNodeConfig(NodeType.AVERAGE, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.LT, 30.0, 60)
        ));

        List<ExecutableFlow> result = service.prepareAndFilterDispatchableFlows(context, List.of(flow1, flow2));

        assertThat(result).containsExactly(flow1, flow2);
        verify(timeSeriesRepository).save(
                eq(ROOM_ID),
                eq(MeasurementType.TEMPERATURE),
                eq(26.5),
                eq(ROOM_UPDATED_AT)
        );
        verify(flowFailureEventPublisher, never()).publish(any());
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
