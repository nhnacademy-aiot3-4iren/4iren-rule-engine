package com.nhnacademy.ruleengine.domain.nodeconfig.enums;

// SensorType.java
public enum SensorType {
    CO2("이산화탄소"),
    HUMIDITY("상대습도"),
    ILLUMINATION("주변 조도"),
    INFRARED("적외선"),
    PRESSURE("대기압"),
    TEMPERATURE("온도"),
    TVOC("총휘발성유기화합물 농도")
    ;

    private String sensorDesc;

    SensorType(String sensorDesc){
        this.sensorDesc = sensorDesc;
    }
}
