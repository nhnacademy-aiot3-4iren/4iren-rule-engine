package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
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
        String targetDeviceEui,

        @NotNull
        Operator operator,

        @NotNull
        Double average,

        @NotNull
        @Positive
        Integer windowSec


) implements NodeConfig {
}
