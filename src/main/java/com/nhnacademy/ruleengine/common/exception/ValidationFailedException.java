package com.nhnacademy.ruleengine.common.exception;

import com.nhnacademy.ruleengine.common.advice.ValidationErrorResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationFailedException extends RuntimeException {
    private final transient ErrorCode errorCode;
    private final transient List<ValidationErrorResponse.ValidationError> errors;

    public ValidationFailedException(List<ValidationErrorResponse.ValidationError> errors, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errors = errors;
    }
}
