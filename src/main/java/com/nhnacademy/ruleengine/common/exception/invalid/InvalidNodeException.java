package com.nhnacademy.ruleengine.common.exception.invalid;

public class InvalidNodeException extends RuntimeException {
    public InvalidNodeException(Long nodeId) {
        super("Invalid Node: " + nodeId);
    }
}
