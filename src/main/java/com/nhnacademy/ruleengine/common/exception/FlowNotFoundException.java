package com.nhnacademy.ruleengine.common.exception;

public class FlowNotFoundException extends RuntimeException {
    public FlowNotFoundException(Long flowId) {
        super("Flow Not Found: "+ flowId);
    }
}
