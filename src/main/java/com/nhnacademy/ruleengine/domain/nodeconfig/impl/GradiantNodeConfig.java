package com.nhnacademy.ruleengine.domain.nodeconfig.impl;

import com.nhnacademy.ruleengine.common.enums.GradiantDirection;
import com.nhnacademy.ruleengine.common.enums.Operator;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
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
        Operator operator,
        @NotNull
        double gradiant,
        @NotNull
        @Positive
        Integer windowSec,

        String unit,

        GradiantDirection direction,

        String targetSensor
)implements NodeConfig {
}
