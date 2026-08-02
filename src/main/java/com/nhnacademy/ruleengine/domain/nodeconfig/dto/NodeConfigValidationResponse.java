package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import java.util.List;

public record NodeConfigValidationResponse(
        boolean valid,
        List<ValidationErrorResponse> errors
) {
    public static NodeConfigValidationResponse success() {
        return new NodeConfigValidationResponse(true, List.of());
    }

    public static NodeConfigValidationResponse fail(List<ValidationErrorResponse> errors) {
        return new NodeConfigValidationResponse(false, errors);
    }
}