package com.nhnacademy.ruleengine.domain.nodeconfig.impl.condition;


import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import com.nhnacademy.ruleengine.domain.nodeconfig.NodeConfig;

public record ThresholdNodeConfig (

        NodeType nodeType,

        Integer x,

        Integer y,

        SensorType sensorType,

        String targetSensor,

        Operator operator,

        double threshold,

        String unit

)implements NodeConfig {

}
