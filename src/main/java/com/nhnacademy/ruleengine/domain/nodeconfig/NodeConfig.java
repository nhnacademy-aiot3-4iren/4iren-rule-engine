package com.nhnacademy.ruleengine.domain.nodeconfig;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.impl.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.impl.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.impl.GradiantNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.impl.ThresholdNodeConfig;

import java.time.Duration;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "nodeType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ThresholdNodeConfig.class, name = "THRESHOLD"),
        @JsonSubTypes.Type(value = GradiantNodeConfig.class, name = "GRADIENT"),
        @JsonSubTypes.Type(value = AverageNodeConfig.class, name = "AVERAGE"),
        @JsonSubTypes.Type(value = Duration.class, name = "DURATION"),

        @JsonSubTypes.Type(value = AlertNodeConfig.class, name = "ALERT")
})
public interface NodeConfig {
    // 공통 필드 있으면 여기
    NodeType nodeType();
    Integer x();
    Integer y();
}