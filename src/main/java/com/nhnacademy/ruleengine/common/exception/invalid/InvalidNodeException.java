package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidNodeException extends BaseException {
    public InvalidNodeException() {
        super(ErrorCode.INVALID_NODE);
    }
}
