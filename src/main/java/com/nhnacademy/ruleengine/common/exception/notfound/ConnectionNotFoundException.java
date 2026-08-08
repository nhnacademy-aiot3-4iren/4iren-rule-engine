package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class ConnectionNotFoundException extends BusinessException {
    public ConnectionNotFoundException() {
        super(ErrorCode.CONNECTION_NOT_FOUND);
    }
}
