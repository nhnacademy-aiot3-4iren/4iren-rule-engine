package com.nhnacademy.ruleengine.common.exception.notfound;

public class NodeNotFoundException extends RuntimeException {
    public NodeNotFoundException(Long nodeId) {
        super("Node NotFound: " + nodeId);
    }
}
