package com.nhnacademy.ruleengine.common.exception.notfound;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;

public class SensorTypeNotFoundException extends BusinessException {
    public SensorTypeNotFoundException(String externalSensorType) {
        super( "SensorType Not Found: "+ externalSensorType);
    }
}
