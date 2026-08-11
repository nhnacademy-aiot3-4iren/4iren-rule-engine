package com.nhnacademy.ruleengine.common.exception.invalid;

import lombok.Getter;

import java.util.List;

@Getter
public class FlowValidationFailed extends RuntimeException {
    private final List<String> errors;


    public FlowValidationFailed(List<String> errors) {
        super("플로우 무결성 검증 실패");
        this.errors = errors;
    }

}
