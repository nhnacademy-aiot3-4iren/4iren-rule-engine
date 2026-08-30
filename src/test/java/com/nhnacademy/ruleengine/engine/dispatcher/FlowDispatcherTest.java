package com.nhnacademy.ruleengine.engine.dispatcher;

import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.FlowExecutor;
import com.nhnacademy.ruleengine.engine.filter.FlowScheduleFilter;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowDispatcherTest {


    @Mock
    private FlowScheduleFilter filter;

    @Mock
    private FlowExecutor flowExecutor;

    private ExecutorService executorService;
    private FlowDispatcher dispatcher;

    @Mock
    private EnvironmentContext context;
    @BeforeEach
    void setUp() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        dispatcher = new FlowDispatcher(
                executorService,
                filter,
                flowExecutor
        );

    }

    @AfterEach
    void tearDown() throws Exception {
        executorService.shutdown();
    }

    @Test
    @DisplayName("스케줄 필터링 결과 false - FlowExecutor를 실행")
    void dispatch_executesSchedulableFlow(){
        ExecutableFlow flow = createFlow(1L);

        when(filter.isSchedulable(flow)).thenReturn(true);

        CompletableFuture<Void> future = dispatcher.dispatch(List.of(flow), context);
        future.join();

        verify(filter, times(1)).isSchedulable(flow);
        verify(flowExecutor, times(1)).execute(argThat(contextMatches(flow, context)));
    }

    @Test
    @DisplayName("스케줄 필터링 결과 false - FlowExecutor 실행 안함")
    void dispatch_skipsUnschedulableFlow(){
        ExecutableFlow flow = createFlow(1L);

        when(filter.isSchedulable(flow)).thenReturn(false);

        CompletableFuture<Void> future =dispatcher.dispatch(List.of(flow), context);
        future.join();

        verify(filter, times(1)).isSchedulable(flow);
        verify(flowExecutor, never()).execute(any());
    }

    @Test
    @DisplayName("여러 flow를 모두 dispatch 한다")
    void dispatch_executesAllSchedulableFlows() {
        ExecutableFlow flow1 = createFlow(1L);
        ExecutableFlow flow2 = createFlow(2L);
        ExecutableFlow flow3 = createFlow(3L);

        when(filter.isSchedulable(flow1)).thenReturn(true);
        when(filter.isSchedulable(flow2)).thenReturn(true);
        when(filter.isSchedulable(flow3)).thenReturn(true);

        CompletableFuture<Void> future = dispatcher.dispatch(List.of(flow1, flow2, flow3), context);
        future.join();

        verify(flowExecutor, times(1)).execute(argThat(contextMatches(flow1, context)));
        verify(flowExecutor, times(1)).execute(argThat(contextMatches(flow2, context)));
        verify(flowExecutor, times(1)).execute(argThat(contextMatches(flow3, context)));
    }

    @Test
    @DisplayName("여러 flow 중 스케줄 필터링 true인 flow만 실행")
    void dispatch_executesOnlySchedulableFlow(){
        ExecutableFlow flow1 = createFlow(1L);
        ExecutableFlow flow2 = createFlow(2L);
        ExecutableFlow flow3 = createFlow(3L);

        when(filter.isSchedulable(flow1)).thenReturn(true);
        when(filter.isSchedulable(flow2)).thenReturn(false);
        when(filter.isSchedulable(flow3)).thenReturn(true);

        CompletableFuture<Void> future = dispatcher.dispatch(List.of(flow1, flow2, flow3), context);
        future.join();

        verify(flowExecutor, times(1)).execute(argThat(contextMatches(flow1, context)));
        verify(flowExecutor, never()).execute(argThat(contextMatches(flow2, context)));
        verify(flowExecutor, times(1)).execute(argThat(contextMatches(flow3, context)));

    }

    @Test
    @DisplayName("한 flow 실행 중 예외가 발생해도 dispatch 전체는 완료된다")
    void dispatch_completesEvenWhenOneFlowFails() {
        ExecutableFlow flow1 = createFlow(1L);
        ExecutableFlow flow2 = createFlow(2L);

        when(filter.isSchedulable(flow1)).thenReturn(true);
        when(filter.isSchedulable(flow2)).thenReturn(true);

        doThrow(new RuntimeException("boom"))
                .when(flowExecutor)
                .execute(argThat(context1 -> context1.flow().flowId().equals(1L)));

        CompletableFuture<Void> future = dispatcher.dispatch(List.of(flow1, flow2), context);
        future.join();

        verify(flowExecutor,times(1)).execute(argThat(contextMatches(flow1, context)));
        verify(flowExecutor,times(1)).execute(argThat(contextMatches(flow2, context)));


    }
    //helper
    private ArgumentMatcher<FlowContext> contextMatches(
            ExecutableFlow executableFlow,
            EnvironmentContext environmentContext
    ){
        return context ->
                context != null
                && context.flow().equals(executableFlow)
                && context.environmentContext().equals(environmentContext)
                && context.triggeredAt() != null;

    }
    private ExecutableFlow createFlow(Long flowId){
        ExecutableFlow.ExecutableSchedule schedule = new ExecutableFlow.ExecutableSchedule(
                DayOfWeek.MONDAY,
                LocalTime.of(9,0),
                LocalTime.of(18,0)
        );

        return ExecutableFlow.builder()
                .flowId(flowId)
                .flowName("flow-" + flowId)
                .roomId(100L)
                .schedules(List.of(schedule))
                .startNodeId(0L).nodeMap(Map.of())
                .trueAdjacencyMap(Map.of())
                .falseAdjacencyMap(Map.of())
                .build();
    }

}