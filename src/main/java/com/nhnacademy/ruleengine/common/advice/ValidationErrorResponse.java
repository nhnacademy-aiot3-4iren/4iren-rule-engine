package com.nhnacademy.ruleengine.common.advice;

import java.util.List;

public record ValidationErrorResponse(
        String errorMessage,
        List<String> errors
) {
}
