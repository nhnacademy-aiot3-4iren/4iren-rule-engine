package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidPayloadException extends BusinessException {

    // 기본 메시지 사용
    public InvalidPayloadException() {
        super(ErrorCode.INVALID_PAYLOAD);
    }

    // 원인 예외(e)를 함께 감싸서 던질 때 사용
    public InvalidPayloadException(String customMessage, Throwable cause) {
        super(ErrorCode.INVALID_PAYLOAD);
    }
}