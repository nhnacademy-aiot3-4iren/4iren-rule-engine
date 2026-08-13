package com.nhnacademy.ruleengine.engine.dispatcher;

import com.nhnacademy.ruleengine.engine.filter.FlowScheduleFilter;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.SensorPayload;
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
//    private final FlowExecutor flowExecutor;

    public CompletableFuture<Void> dispatch(List<ExecutableFlow> flows, SensorPayload payload) {
        Instant triggeredAt = Instant.now();

        List<CompletableFuture<Void>> futures = flows.stream()
                .map(flow -> CompletableFuture
                        .runAsync(() -> runFlowPipeline(flow, payload, triggeredAt), flowExecutorService)
                        .exceptionally(ex -> {
                            log.error("flow({}) 파이프라인 실행 중 처리되지 않은 예외 발생", flow.flowId(), ex);
                            return null;
                        }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void runFlowPipeline(ExecutableFlow flow, SensorPayload payload, Instant triggeredAt) {
        if(!flowScheduleFilter.isSchedulable(flow)) {
            log.debug("flow({}) - 스케줄 조건 불일치, 실행 스킵", flow.flowId());
            return;
        }

//        flowExecutor.execute(flow, payload, triggeredAt);
    }
}
