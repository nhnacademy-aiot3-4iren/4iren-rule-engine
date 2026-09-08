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
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                flowExecutor,
                100
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
    @DisplayName("한 flow 실행 중 예외가 발생하면 모든 flow를 시도한 뒤 dispatch를 실패로 완료한다")
    void dispatch_failsAfterAttemptingAllFlowsWhenOneFlowFails() {
        ExecutableFlow flow1 = createFlow(1L);
        ExecutableFlow flow2 = createFlow(2L);

        when(filter.isSchedulable(flow1)).thenReturn(true);
        when(filter.isSchedulable(flow2)).thenReturn(true);

        doThrow(new RuntimeException("boom"))
                .when(flowExecutor)
                .execute(argThat(context1 -> context1.flow().flowId().equals(1L)));

        CompletableFuture<Void> future = dispatcher.dispatch(List.of(flow1, flow2), context);
        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        verify(flowExecutor,times(1)).execute(argThat(contextMatches(flow1, context)));
        verify(flowExecutor,times(1)).execute(argThat(contextMatches(flow2, context)));
    }

    @Test
    @DisplayName("설정된 최대 동시 실행 수만큼만 flow를 실행한다")
    void dispatch_limitsConcurrentFlowExecution() {
        FlowDispatcher limitedDispatcher = new FlowDispatcher(
                executorService,
                filter,
                flowExecutor,
                1
        );
        ExecutableFlow flow1 = createFlow(1L);
        ExecutableFlow flow2 = createFlow(2L);
        AtomicInteger runningCount = new AtomicInteger();
        AtomicInteger maxRunningCount = new AtomicInteger();

        when(filter.isSchedulable(flow1)).thenReturn(true);
        when(filter.isSchedulable(flow2)).thenReturn(true);
        doAnswer(invocation -> {
            int running = runningCount.incrementAndGet();
            maxRunningCount.accumulateAndGet(running, Math::max);
            Thread.sleep(50);
            runningCount.decrementAndGet();
            return null;
        }).when(flowExecutor).execute(any());

        CompletableFuture<Void> future = limitedDispatcher.dispatch(List.of(flow1, flow2), context);
        future.join();

        verify(flowExecutor, times(2)).execute(any());
        org.assertj.core.api.Assertions.assertThat(maxRunningCount.get()).isEqualTo(1);
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
