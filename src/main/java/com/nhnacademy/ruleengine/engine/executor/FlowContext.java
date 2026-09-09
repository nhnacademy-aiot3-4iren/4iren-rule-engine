package com.nhnacademy.ruleengine.engine.executor;

import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;

import java.time.Instant;

//하나의 플로우를 실행하는 동안 노드 Executor들이 공통으로 참조하는 실행 문맥
public record FlowContext (
        ExecutableFlow flow,
        EnvironmentContext environmentContext
//        List <AlertEvent.NodeResult> nodeResultList
) {
    public Long flowId() {
        return flow.flowId();
    }

    public Long roomId() {
        return flow.roomId();
    }

    /**
     * 이번 룰 엔진 실행을 발생시킨 센서 페이로드의 업데이트 시각을 반환한다.
     */
    public Instant roomStateUpdatedAt() {
        return environmentContext.updatedAt();
    }


    public static FlowContext of(ExecutableFlow flow, EnvironmentContext environmentContext) {
        return new FlowContext(flow, environmentContext);
    }
}
