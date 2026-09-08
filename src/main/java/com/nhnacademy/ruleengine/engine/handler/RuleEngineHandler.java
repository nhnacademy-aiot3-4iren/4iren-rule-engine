package com.nhnacademy.ruleengine.engine.handler;

import com.nhnacademy.ruleengine.engine.dispatcher.FlowDispatcher;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.flow.FlowLoader;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngineHandler {
    private final FlowLoader flowLoader;
    private final FlowDispatcher dispatcher;

    public CompletableFuture<Void> process(EnvironmentContext environmentContext){
        Long roomId = environmentContext.roomId();

        log.info("룰 엔진 처리 시작 roomId={}, 측정값수={}", roomId, environmentContext.metrics().size());

        //1. 플로우 로드
        List<ExecutableFlow> flows = flowLoader.load(roomId);
        if(flows.isEmpty()){
            log.info("실행할 활성 플로우 없음 roomId={}", roomId);
            return CompletableFuture.completedFuture(null);//실제 비동기 실행을 하지 않고, "성공적으로 끝남" 상태의 CompletableFuture를 즉시 반환
        }
        log.info("실행 대상 플로우 로드 완료 roomId={}, flowCount={}", roomId, flows.size());

        //플로우가 존재하면 dispatch호출 뒤 전체 실행 성공/실패 여부를 로깅
        return dispatcher.dispatch(flows, environmentContext).whenComplete((r, ex)->{
            if(ex != null){
                log.error("roomId={} 룰 엔진 플로우 실행 중 최종 실패 발생", roomId, ex);
                return;
            }
            log.info("룰 엔진 처리 완료 roomId={}, flowCount={}", roomId, flows.size());
        });
    }
}
