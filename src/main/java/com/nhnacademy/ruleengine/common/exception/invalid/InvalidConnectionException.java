package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidConnectionException extends BaseException {
    public InvalidConnectionException(Long sourceNodeId, Long targetNodeId) {
        super(ErrorCode.INVALID_CONNECTION);
    }
}
