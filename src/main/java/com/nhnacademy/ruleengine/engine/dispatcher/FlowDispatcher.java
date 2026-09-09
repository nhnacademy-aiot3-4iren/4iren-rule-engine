package com.nhnacademy.ruleengine.engine.dispatcher;

import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.FlowExecutor;
import com.nhnacademy.ruleengine.engine.filter.FlowScheduleFilter;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    /**
     * 한 방(room)에 연결된 실행 대상 플로우들을 비동기로 실행한다.
     * 각 플로우 실행은 개별 Future로 관리하고, 반환 Future는 모든 플로우가 끝났을 때 완료된다.
     */
    public CompletableFuture<Void> dispatch(List<ExecutableFlow> flows, EnvironmentContext environmentContext) {
        log.info("플로우 비동기 실행 시작 roomId={}, flowCount={}", environmentContext.roomId(), flows.size());

        List<CompletableFuture<Void>> futures = flows.stream()
                .map(flow -> runAsyncWithConcurrencyLimit(flow, environmentContext)
                        // 개별 플로우에서 예외가 나면 로그만 남기고, 실패 상태는 Future에 그대로 전파한다.
                        .whenComplete((r, ex) -> {
                            if (ex == null) {
                                return;
                            }
                            log.warn("플로우 파이프라인 실행 실패 flowId={}, roomId={}", flow.flowId(), flow.roomId(), ex);
                        }))
                .toList();

        // 모든 플로우 Future를 하나로 묶어, 호출자가 방 단위 룰 처리 완료 시점을 알 수 있게 한다.
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * 동시에 실행되는 플로우 수를 제한한 뒤 ExecutorService에 실제 실행을 위임한다.
     */
    private CompletableFuture<Void> runAsyncWithConcurrencyLimit(
            ExecutableFlow flow,
            EnvironmentContext environmentContext
    ) {
        // runAsync를 제출하기 전에 permit을 먼저 얻어서, 큐에 무제한으로 작업이 쌓이는 것을 막는다.
        acquireFlowExecutionPermit(flow);
        try {
            return CompletableFuture
                    .runAsync(() -> runFlowPipeline(flow, environmentContext), flowExecutorService)
                    .whenComplete((r, ex) ->
                            // 성공/실패와 관계없이 실행 슬롯을 반드시 반납한다.
                            flowExecutionSemaphore.release()
                    );
        } catch (RuntimeException e) {
            // runAsync 제출 자체가 실패한 경우에는 whenComplete가 실행되지 않으므로 여기서 직접 반납한다.
            flowExecutionSemaphore.release();
            throw e;
        }
    }

    /**
     * 비동기 작업을 실행하기 전 Semaphore에서 실행 권한을 얻는다.
     */
    private void acquireFlowExecutionPermit(ExecutableFlow flow) {
        try {
            flowExecutionSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("플로우 실행 슬롯 획득 중 인터럽트 발생 flowId=%d".formatted(flow.flowId()), e);
        }
    }

    /**
     * 단일 플로우의 스케줄 조건을 확인하고 실제 노드 실행 파이프라인을 시작한다.
     */
    private void runFlowPipeline(ExecutableFlow flow, EnvironmentContext environmentContext) {
        if(!flowScheduleFilter.isSchedulable(flow)) {
            log.info("flow({}) - 스케줄 조건 불일치, 실행 스킵", flow.flowId());
            return;
        }
        // FlowContext는 노드 실행 중 필요한 플로우 정보와 현재 센서 페이로드를 함께 들고 다니는 실행 문맥이다.
        FlowContext context = FlowContext.of(flow, environmentContext);
        log.info("플로우 실행 시작 flowId={}, roomId={}", flow.flowId(), flow.roomId());

        flowExecutor.execute(context);
        log.info("플로우 실행 완료 flowId={}, roomId={}", flow.flowId(), flow.roomId());
    }
}
