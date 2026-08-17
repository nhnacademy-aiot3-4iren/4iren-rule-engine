package com.nhnacademy.ruleengine.engine.executor;

import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;

import java.time.Instant;

public record FlowContext (
        Long flowId,
        Long roomId,
        EnvironmentContext environmentContext,
        Instant triggeredAt
//        List <AlertEvent.NodeResult> nodeResultList
) {


    public static FlowContext of(Long flowId, Long roomId, EnvironmentContext environmentContext, Instant triggeredAt) {
        return new FlowContext(flowId, roomId, environmentContext, triggeredAt);
    }


}
