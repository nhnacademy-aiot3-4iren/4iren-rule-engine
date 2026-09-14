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
    private final TimeSeriesPreparationService timeSeriesPreparationService;
    private final FlowDispatcher dispatcher;

    /**
     * 센서 페이로드 하나를 받아 해당 방의 활성 플로우를 로드하고 룰 엔진 실행을 시작한다.
     */
    public CompletableFuture<Void> process(EnvironmentContext environmentContext){
        Long roomId = environmentContext.roomId();

        log.info("룰 엔진 처리 시작 roomId={}, 측정값수={}", roomId, environmentContext.metrics().size());

        // 현재 방에 연결되어 있고 활성화된 플로우 정의들을 실행 가능한 형태로 불러온다.
        List<ExecutableFlow> flows = flowLoader.load(roomId);
        if(flows.isEmpty()){
            log.info("실행할 활성 플로우 없음 roomId={}", roomId);
            // 실제 비동기 작업을 만들지 않고, 호출자에게는 정상 완료된 Future를 돌려준다.
            return CompletableFuture.completedFuture(null);
        }
        log.debug("실행 대상 플로우 로드 완료 roomId={}, flowCount={}", roomId, flows.size());

        // Average/Duration/Gradient 같은 시간 윈도우 조건이 나중에 조회할 수 있도록 현재 센서값을 먼저 저장한다.
        // 시계열 저장 실패는 영향을 받는 플로우만 실패 이벤트로 분리하고, 나머지 플로우는 계속 실행한다.
        List<ExecutableFlow> dispatchableFlows = timeSeriesPreparationService.prepareAndFilterDispatchableFlows(environmentContext, flows);
        if(dispatchableFlows.isEmpty()){
            log.info("시계열 준비 후 실행 가능한 플로우 없음 roomId={}", roomId);
            return CompletableFuture.completedFuture(null);
        }

        // 플로우가 존재하면 dispatcher에 비동기 실행을 맡기고, 방 단위 처리 결과만 여기서 로깅한다.
        return dispatcher.dispatch(dispatchableFlows, environmentContext).whenComplete((r, ex)->{
            if(ex != null){
                log.error("roomId={} 룰 엔진 플로우 실행 중 최종 실패 발생", roomId, ex);
                return;
            }
            log.info("룰 엔진 처리 완료 roomId={}, flowCount={}", roomId, dispatchableFlows.size());
        });
    }
}
