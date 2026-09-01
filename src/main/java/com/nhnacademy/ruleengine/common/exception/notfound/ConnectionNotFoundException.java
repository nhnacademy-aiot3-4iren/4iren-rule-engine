package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class ConnectionNotFoundException extends BaseException {
    public ConnectionNotFoundException() {
        super(ErrorCode.CONNECTION_NOT_FOUND);
    }
}
