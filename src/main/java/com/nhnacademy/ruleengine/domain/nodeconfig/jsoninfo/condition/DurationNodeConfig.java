package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import jakarta.validation.constraints.NotNull;

public record DurationNodeConfig(
        @NotNull
        NodeType nodeType,

        @NotNull
        Integer x,

        @NotNull
        Integer y,

        @NotNull
        MeasurementType measurementType,

        @NotNull
        String unit,

        @NotNull
        Operator operator,

        @NotNull
        Double threshold,

        @NotNull
        Integer durationSec


) implements NodeConfig {
        @Override
        public MeasurementType measurementType(){
                return measurementType;
        }
}
