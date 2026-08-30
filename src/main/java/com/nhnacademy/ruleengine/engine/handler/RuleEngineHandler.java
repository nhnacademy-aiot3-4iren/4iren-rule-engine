package com.nhnacademy.ruleengine.engine.handler;

import com.nhnacademy.ruleengine.engine.dispatcher.FlowDispatcher;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.flow.FlowLoader;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleEngineHandler {
    private final FlowLoader flowLoader;
    private final FlowDispatcher dispatcher;

    public void process(EnvironmentContext environmentContext){
        Long roomId = environmentContext.roomId();

        log.info("센서 데이터 수신 roomId={}", roomId);

        //1. 플로우 로드
        List<ExecutableFlow> flows = flowLoader.load(roomId);
        if(flows.isEmpty()){
            return;
        }

        dispatcher.dispatch(flows, environmentContext).whenComplete((r, ex)->{
            if(ex != null){
                log.error("roomId={} 룰 엔진 플로우 실행 중 최종 실패 발생", roomId, ex);
            }
        });

    }
}
