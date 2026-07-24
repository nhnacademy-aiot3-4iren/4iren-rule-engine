package com.nhnacademy.ruleengine.common.exception.unauthorized;

public class UnauthorizedFlowAccessException extends RuntimeException {
    public UnauthorizedFlowAccessException(Long flowId, Long roomId) {
        super("Unauthoriwed Flow: " + flowId + "In roomId: " +roomId );
    }
}
