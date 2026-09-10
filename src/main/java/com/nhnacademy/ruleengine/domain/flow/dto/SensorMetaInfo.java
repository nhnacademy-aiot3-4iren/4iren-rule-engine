package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "센서 측정 메타 정보")
public record SensorMetaInfo(
        @Schema(description = "해당 강의실에서 측정가능한 타입", example = "TEMPERATURE")
        MeasurementType measurementType,
        @Schema(description = "화면 표시 이름", example = "온도")
        String displayName,
        @Schema(description = "측정 항목 설명", example = "강의실 온도")
        String description,
        @Schema(description = "단위 기호", example = "C")
        String symbol
) {
    public static SensorMetaInfo of(MeasurementType measurementType, MetricCatalogInfo catalogInfo) {
        return new SensorMetaInfo(
                measurementType,
                catalogInfo.displayName(),
                catalogInfo.description(),
                catalogInfo.symbol()
        );
    }
}
