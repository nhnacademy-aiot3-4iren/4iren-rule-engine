package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidFlowException extends BaseException {
    public InvalidFlowException() {
        super(ErrorCode.INVALID_FLOW);
    }
}
