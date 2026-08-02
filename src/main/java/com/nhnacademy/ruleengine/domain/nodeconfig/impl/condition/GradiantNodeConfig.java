package com.nhnacademy.ruleengine.domain.nodeconfig.impl.condition;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.GradiantDirection;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GradiantNodeConfig(

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
        double gradiant,
        @NotNull
        @Positive
        Integer windowSec,

        @NotNull
        GradiantDirection direction

)implements NodeConfig {
}
