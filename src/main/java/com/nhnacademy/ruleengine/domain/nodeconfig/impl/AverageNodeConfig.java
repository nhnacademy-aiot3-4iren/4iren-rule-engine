package com.nhnacademy.ruleengine.domain.nodeconfig.impl;

import com.nhnacademy.ruleengine.common.enums.Operator;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AverageNodeConfig(
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
        Double average,

        @NotNull
        @Positive
        Integer windowSec,

        String unit
) implements NodeConfig {
}
