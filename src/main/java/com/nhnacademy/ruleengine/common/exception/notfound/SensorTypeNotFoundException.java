package com.nhnacademy.ruleengine.common.exception.notfound;

public class SensorTypeNotFoundException extends RuntimeException {
    public SensorTypeNotFoundException(String externalSensorType) {
        super( "SensorType Not Found: "+ externalSensorType);
    }
}
