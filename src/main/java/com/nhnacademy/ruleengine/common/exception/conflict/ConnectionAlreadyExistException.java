package com.nhnacademy.ruleengine.common.exception.conflict;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class ConnectionAlreadyExistException extends BusinessException {
    public ConnectionAlreadyExistException() {
        super(ErrorCode.CONNECTION_ALREADY_EXISTS);
    }
}
