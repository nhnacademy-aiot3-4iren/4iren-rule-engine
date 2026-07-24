package com.nhnacademy.ruleengine.common.exception.notfound;

public class ConnectionNotFoundException extends RuntimeException {
    public ConnectionNotFoundException(Long connId) {
        super("Connection NotFound: " + connId);
    }
}
