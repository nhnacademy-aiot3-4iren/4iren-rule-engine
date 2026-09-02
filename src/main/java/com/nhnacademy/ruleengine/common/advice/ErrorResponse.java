package com.nhnacademy.ruleengine.common.advice;

public record ErrorResponse(
        String code,
        String message
) {
}
