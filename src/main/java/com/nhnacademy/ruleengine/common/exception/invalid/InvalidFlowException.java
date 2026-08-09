package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidFlowException extends BusinessException {
    public InvalidFlowException() {
        super(ErrorCode.INVALID_FLOW);
    }
}
