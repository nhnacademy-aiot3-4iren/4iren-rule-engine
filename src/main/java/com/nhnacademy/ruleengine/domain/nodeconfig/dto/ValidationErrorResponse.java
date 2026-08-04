package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

public record ValidationErrorResponse(
        String field,
        String message
) {
    public static ValidationErrorResponse of(String field, String message){
        return new ValidationErrorResponse(field, message);
    }
}
