package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidPayloadException extends BusinessException {

    public InvalidPayloadException() {
        super(ErrorCode.INVALID_PAYLOAD);
    }

    public InvalidPayloadException(String customMessage, Throwable cause) {
        super(ErrorCode.INVALID_PAYLOAD);
        if (cause != null) {
            initCause(cause);
        }
    }
}