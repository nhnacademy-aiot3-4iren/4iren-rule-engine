package com.nhnacademy.ruleengine.engine.dispatcher;

import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.FlowExecutor;
import com.nhnacademy.ruleengine.engine.filter.FlowScheduleFilter;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
public class FlowDispatcher {

    private final ExecutorService flowExecutorService;
    private final FlowScheduleFilter flowScheduleFilter;
    private final FlowExecutor flowExecutor;
    private final Semaphore flowExecutionSemaphore;
    private int flowMaxConcurrency;

    public FlowDispatcher(
            ExecutorService flowExecutorService,
            FlowScheduleFilter flowScheduleFilter,
            FlowExecutor flowExecutor,
            @Value("${ruleengine.flow.max-concurrency:100}") int flowMaxConcurrency
    ) {
        if (flowMaxConcurrency < 1) {
            throw new IllegalArgumentException("ruleengine.flow.max-concurrency must be greater than 0");
        }
        this.flowExecutorService = flowExecutorService;
        this.flowScheduleFilter = flowScheduleFilter;
        this.flowExecutor = flowExecutor;
        this.flowExecutionSemaphore = new Semaphore(flowMaxConcurrency);
    }

    public CompletableFuture<Void> dispatch(List<ExecutableFlow> flows, EnvironmentContext environmentContext) {
        Instant triggeredAt = Instant.now();
        log.info("플로우 비동기 실행 시작 roomId={}, flowCount={}", environmentContext.roomId(), flows.size());

        List<CompletableFuture<Void>> futures = flows.stream()
                .map(flow -> runAsyncWithConcurrencyLimit(flow, environmentContext, triggeredAt)
                        //플로우 예외 로그 남기고 future는 실패 응답
                        .whenComplete((r, ex) -> {
                            if (ex == null) {
                                return;
                            }
                            log.warn("플로우 파이프라인 실행 실패 flowId={}, roomId={}", flow.flowId(), flow.roomId(), ex);
                        }))
                .toList();

        //인자로 전달된 모든 비동기 작업이 완료될 때까지 대기하는 새로운 CompletableFuture<Void> 반환 (각각의 비동기 실행들을 감시하다 모든 작업이 끝났을 때 Done상태로 바뀜)
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> runAsyncWithConcurrencyLimit(
            ExecutableFlow flow,
            EnvironmentContext environmentContext,
            Instant triggeredAt
    ) {
        acquireFlowExecutionPermit(flow);
        try {
            return CompletableFuture
                    .runAsync(() -> runFlowPipeline(flow, environmentContext, triggeredAt), flowExecutorService)
                    .whenComplete((r, ex) ->
                            flowExecutionSemaphore.release() //슬롯 반납
                    );
        } catch (RuntimeException e) {
            flowExecutionSemaphore.release();
            throw e;
        }
    }

    //비동기 작업을 실행하기 전 Semaphore.acquire()를 통해 실행 권한을 요청 및 대기
    private void acquireFlowExecutionPermit(ExecutableFlow flow) {
        try {
            flowExecutionSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("플로우 실행 슬롯 획득 중 인터럽트 발생 flowId=%d".formatted(flow.flowId()), e);
        }
    }

    private void runFlowPipeline(ExecutableFlow flow, EnvironmentContext environmentContext, Instant triggeredAt) {
        if(!flowScheduleFilter.isSchedulable(flow)) {
            log.info("flow({}) - 스케줄 조건 불일치, 실행 스킵", flow.flowId());
            return;
        }
        FlowContext context = FlowContext.of(flow, environmentContext, triggeredAt);
        log.info("플로우 실행 시작 flowId={}, roomId={}", flow.flowId(), flow.roomId());

        flowExecutor.execute(context);
        log.info("플로우 실행 완료 flowId={}, roomId={}", flow.flowId(), flow.roomId());
    }
}
