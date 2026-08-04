package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class MeasurementTypeNotFoundException extends BusinessException {
    public MeasurementTypeNotFoundException() {
        super(ErrorCode.MEASUREMENT_TYPE_NOT_FOUND);
    }
}
