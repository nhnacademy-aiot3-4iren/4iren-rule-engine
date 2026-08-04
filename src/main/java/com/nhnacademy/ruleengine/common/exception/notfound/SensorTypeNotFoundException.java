package com.nhnacademy.ruleengine.common.exception.notfound;

public class SensorTypeNotFoundException extends RuntimeException {
    public SensorTypeNotFoundException(String externalSensorType) {
        super( "MeasurementType Not Found: "+ externalSensorType);
    }
}
