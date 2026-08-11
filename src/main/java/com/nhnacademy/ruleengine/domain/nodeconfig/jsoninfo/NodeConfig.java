package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.AverageNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.DurationNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.GradientNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;



@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "nodeType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ThresholdNodeConfig.class, name = "THRESHOLD"),
        @JsonSubTypes.Type(value = GradientNodeConfig.class, name = "GRADIENT"),
        @JsonSubTypes.Type(value = AverageNodeConfig.class, name = "AVERAGE"),
        @JsonSubTypes.Type(value = DurationNodeConfig.class, name = "DURATION"),

        @JsonSubTypes.Type(value = AlertNodeConfig.class, name = "ALERT")
})
public interface NodeConfig {
    // 공통 필드 있으면 여기
    NodeType nodeType();
    Integer x();
    Integer y();

    default MeasurementType measurementType(){
        return null;
    }
}