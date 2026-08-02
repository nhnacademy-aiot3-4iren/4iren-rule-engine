package com.nhnacademy.ruleengine.domain.nodeconfig.impl.condition;


import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
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
        String targetDeviceEui,

        @NotNull
        Operator operator,

        @NotNull
        double threshold

)implements NodeConfig {

}
