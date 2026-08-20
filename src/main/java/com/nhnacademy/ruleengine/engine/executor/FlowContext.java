package com.nhnacademy.ruleengine.engine.executor;

import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;

import java.time.Instant;

public record FlowContext (
        ExecutableFlow flow,
        EnvironmentContext environmentContext,
        Instant triggeredAt
//        List <AlertEvent.NodeResult> nodeResultList
) {
    public Long flowId() {
        return flow.flowId();
    }

    public Long roomId() {
        return flow.roomId();
    }

    public static FlowContext of(ExecutableFlow flow, EnvironmentContext environmentContext, Instant triggeredAt) {
        return new FlowContext(flow, environmentContext, triggeredAt);
    }
}
