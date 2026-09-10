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
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.logical.OrNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.start.StartNodeConfig;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(
        description = "노드 타입별 설정 정보",
        discriminatorProperty = "nodeType",//구현체 매핑 기준 필드
        oneOf = {//NodeConfig자리에 올수 있는 후보 타입 목록 -> Swagger/OpenAPI에게 해당 필드가 단일 고정 dto가 아님을 알림
                ThresholdNodeConfig.class,
                GradientNodeConfig.class,
                AverageNodeConfig.class,
                DurationNodeConfig.class,
                OrNodeConfig.class,
                AlertNodeConfig.class,
                StartNodeConfig.class
        },
        discriminatorMapping = {
                @DiscriminatorMapping(value = "THRESHOLD", schema = ThresholdNodeConfig.class),
                @DiscriminatorMapping(value = "GRADIENT", schema = GradientNodeConfig.class),
                @DiscriminatorMapping(value = "AVERAGE", schema = AverageNodeConfig.class),
                @DiscriminatorMapping(value = "DURATION", schema = DurationNodeConfig.class),
                @DiscriminatorMapping(value = "OR", schema = OrNodeConfig.class),
                @DiscriminatorMapping(value = "ALERT", schema = AlertNodeConfig.class),
                @DiscriminatorMapping(value = "START", schema = StartNodeConfig.class)
        }
)
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

        @JsonSubTypes.Type(value = OrNodeConfig.class, name = "OR"),

        @JsonSubTypes.Type(value = AlertNodeConfig.class, name = "ALERT"),

        @JsonSubTypes.Type(value = StartNodeConfig.class, name = "START")
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
