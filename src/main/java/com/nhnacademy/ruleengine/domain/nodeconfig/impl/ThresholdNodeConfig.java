package com.nhnacademy.ruleengine.domain.nodeconfig.impl;


import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.validation.constraints.NotNull;

public record ThresholdNodeConfig (

        @NotNull
        NodeType nodeType,

        @NotNull
        Integer x,

        @NotNull
        Integer y,

        @NotNull
        SensorType sensorType,
        @NotNull
        Operator operator,
        @NotNull
        double threshold,

        String unit,

        String targetSensor
)implements NodeConfig {

}
