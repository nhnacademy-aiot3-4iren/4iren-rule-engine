package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.advice.ValidationErrorResponse;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;
import com.nhnacademy.ruleengine.common.exception.ValidationFailedException;
import lombok.Getter;

import java.util.List;

@Getter
public class FlowScheduleValidationFailed extends ValidationFailedException {

    public FlowScheduleValidationFailed(List<ValidationErrorResponse.ValidationError> errors) {
        super(errors, ErrorCode.FLOW_SCHEDULE_VALIDATION_FAILED);
    }
}
