package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidNodeException extends BusinessException {
    public InvalidNodeException() {
        super(ErrorCode.INVALID_NODE);
    }
}
