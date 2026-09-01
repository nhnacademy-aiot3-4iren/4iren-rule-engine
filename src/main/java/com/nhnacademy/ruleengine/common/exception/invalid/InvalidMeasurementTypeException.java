package com.nhnacademy.ruleengine.common.exception.invalid;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class InvalidMeasurementTypeException extends BaseException {
    public InvalidMeasurementTypeException() {
        super(ErrorCode.INVALID_MEASUREMENT_TYPE);
    }
}
