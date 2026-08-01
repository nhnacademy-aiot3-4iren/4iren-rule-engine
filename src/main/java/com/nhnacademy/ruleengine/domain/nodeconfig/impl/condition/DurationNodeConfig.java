package com.nhnacademy.ruleengine.domain.nodeconfig.impl.condition;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;

public record DurationNodeConfig(

        NodeType nodeType,

        Integer x,

        Integer y,

        SensorType sensorType,

        Operator operator,

        Double threshold,

        Integer durationSec,

        String unit

) implements NodeConfig {
}
