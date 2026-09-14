package com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition;


import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "임계치 판단 노드 설정")
public record ThresholdNodeConfig (

        @Schema(description = "노드 타입", example = "THRESHOLD")
        @NotNull
        NodeType nodeType,

        @Schema(description = "플로우 편집 화면의 x 좌표", example = "300")
        @NotNull
        Integer x,

        @Schema(description = "플로우 편집 화면의 y 좌표", example = "120")
        @NotNull
        Integer y,

        @Schema(description = "측정 타입", example = "TEMPERATURE")
        @NotNull
        MeasurementType measurementType,

        @Schema(description = "측정 단위", example = "C")
        @NotNull
        String unit,

        @Schema(description = "비교 연산자", example = "GT")
        @NotNull
        Operator operator,

        @Schema(description = "임계값", example = "28.0")
        @NotNull
        Double threshold

)implements NodeConfig {
        @Override
        public MeasurementType measurementType(){
                return measurementType;
        }
}
