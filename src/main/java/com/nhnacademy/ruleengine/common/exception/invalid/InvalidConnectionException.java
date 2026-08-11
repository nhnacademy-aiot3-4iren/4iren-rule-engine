package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.ErrorCode;
import com.nhnacademy.ruleengine.common.exception.BusinessException;

public class InvalidConnectionException extends BusinessException {
    public InvalidConnectionException(Long sourceNodeId, Long targetNodeId) {
        super(ErrorCode.INVALID_CONNECTION);
    }
}
