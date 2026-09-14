package com.nhnacademy.ruleengine.engine.model;

import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;

import java.time.Instant;

//플로우 실행 실패 이벤트
public record FlowFailureEvent(
        Long roomId,
        Long flowId,
        String flowName,
        Instant payloadUpdatedAt,
        Instant failedAt,
        String exceptionType,
        String message
) {
    public static FlowFailureEvent of(ExecutableFlow flow, EnvironmentContext environmentContext, Throwable throwable) {
        return new FlowFailureEvent(
                flow.roomId(),
                flow.flowId(),
                flow.flowName(),
                environmentContext.updatedAt(),
                Instant.now(),
                throwable.getClass().getName(),
                throwable.getMessage()
        );
    }
}
