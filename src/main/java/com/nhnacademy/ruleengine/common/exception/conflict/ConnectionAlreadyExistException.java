package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class ConnectionAlreadyExistException extends BaseException {
    public ConnectionAlreadyExistException() {
        super(ErrorCode.CONNECTION_ALREADY_EXISTS);
    }
}
