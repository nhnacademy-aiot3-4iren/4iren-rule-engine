package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

public record ValidationErrorResponse(
        String field,
        String message
) {
}
