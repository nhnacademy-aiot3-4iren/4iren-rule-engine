package com.nhnacademy.ruleengine.engine.facade;

import com.nhnacademy.ruleengine.common.cache.repository.FlowCacheRepository;
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
public class RuleEngineFacade {
    private final FlowLoader flowLoader;
    private final FlowCacheRepository flowCacheRepository;


    public void process(EnvironmentContext payload){
        Long roomId = payload.roomId();

        log.info("센서 데이터 수신 roomId={}", roomId);

        //1. 플로우 로드
        List<ExecutableFlow> flows = flowLoader.load(roomId);
        if(flows.isEmpty()){
            return;
        }


        //flowDispatcher 호출
        //dispatcher.dispatch(flows, payload);
    }
}
