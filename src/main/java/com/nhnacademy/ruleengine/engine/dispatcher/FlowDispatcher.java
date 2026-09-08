package com.nhnacademy.ruleengine.engine.dispatcher;

import com.nhnacademy.ruleengine.engine.executor.FlowContext;
import com.nhnacademy.ruleengine.engine.executor.FlowExecutor;
import com.nhnacademy.ruleengine.engine.filter.FlowScheduleFilter;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowDispatcher {

    private final ExecutorService flowExecutorService;
    private final FlowScheduleFilter flowScheduleFilter;
    private final FlowExecutor flowExecutor;

    public CompletableFuture<Void> dispatch(List<ExecutableFlow> flows, EnvironmentContext environmentContext) {
        Instant triggeredAt = Instant.now();
        log.info("플로우 비동기 실행 시작 roomId={}, flowCount={}", environmentContext.roomId(), flows.size());

        List<CompletableFuture<Void>> futures = flows.stream()
                .map(flow -> CompletableFuture
                        .runAsync(() -> runFlowPipeline(flow, environmentContext, triggeredAt), flowExecutorService)// runFlowPipeline 작업을 가상 스레드 풀(flowExecutorService)에서 실행하도록 지정
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
