package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.advice.ValidationError;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

import java.util.List;

public class FlowValidationFailed extends RuntimeException {
    private final List<String> errors;


    public FlowValidationFailed(List<String> errors) {
        super("플로우 무결성 검증 실패");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
