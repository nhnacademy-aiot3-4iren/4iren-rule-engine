package com.nhnacademy.ruleengine.common.exception.invalid;

public class InvalidConnectionException extends RuntimeException {
    public InvalidConnectionException(Long sourceNodeId, Long targetNodeId) {
        super("Invalid Connection sourceNodeId:"+ sourceNodeId + " targetNodeId: " + targetNodeId);
    }
}
