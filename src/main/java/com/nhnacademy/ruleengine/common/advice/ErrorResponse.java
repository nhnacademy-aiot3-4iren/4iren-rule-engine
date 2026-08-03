package com.nhnacademy.ruleengine.common.advice;

import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public record ErrorResponse(
        String code,
        String message
) {
}
