package com.nhnacademy.ruleengine.common.exception.notfound;

public class FlowNotFoundException extends RuntimeException {
    public FlowNotFoundException(Long flowId) {
        super("Flow NotFound: "+ flowId);
    }

    public FlowNotFoundException(Long flowId, boolean isTemplate) {
        super("Flow NotFound: "+ flowId);
    }
}
