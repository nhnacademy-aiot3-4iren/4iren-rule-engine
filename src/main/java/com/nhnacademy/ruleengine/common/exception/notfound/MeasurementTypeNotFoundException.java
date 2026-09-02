package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BaseException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class MeasurementTypeNotFoundException extends BaseException {
    public MeasurementTypeNotFoundException() {
        super(ErrorCode.MEASUREMENT_TYPE_NOT_FOUND);
    }
}
