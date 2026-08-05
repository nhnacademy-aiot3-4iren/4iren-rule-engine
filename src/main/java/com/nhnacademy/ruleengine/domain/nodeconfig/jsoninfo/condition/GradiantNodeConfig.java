package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
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
        MeasurementType measurementType,

        @NotNull
        String targetDeviceEui,

        @NotNull
        Operator operator,

        @NotNull
        double gradiant,

        @NotNull
        @Positive
        Integer windowSec

//        gradiant 의 음수 양수 여부로 알 수 있으므로 필드 포함하지 않기로 함
//        @NotNull
//        GradiantDirection direction

)implements NodeConfig {
}
