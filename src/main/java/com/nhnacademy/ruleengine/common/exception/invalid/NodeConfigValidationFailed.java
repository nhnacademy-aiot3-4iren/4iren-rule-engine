package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.advice.ValidationErrorResponse;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;
import com.nhnacademy.ruleengine.common.exception.ValidationFailedException;
import lombok.Getter;

import java.util.List;

@Getter
public class NodeConfigValidationFailed extends ValidationFailedException {

    public NodeConfigValidationFailed(List<ValidationErrorResponse.ValidationError> errors) {
        super(errors, ErrorCode.NODE_CONFIG_VALIDATION_FAILED);
    }
}
