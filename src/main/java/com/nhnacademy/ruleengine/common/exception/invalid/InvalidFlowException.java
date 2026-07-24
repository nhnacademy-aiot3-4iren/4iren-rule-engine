package com.nhnacademy.ruleengine.common.exception.invalid;

public class InvalidFlowException extends RuntimeException {
    public InvalidFlowException(Long flowId) {
        super("Invalid Flow: " + flowId);
    }
}
