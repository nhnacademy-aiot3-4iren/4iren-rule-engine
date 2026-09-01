package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

@Builder
// 반환할 레코드 생성 (없다면)
public record SensorMetaInfo(
        MeasurementType measurementType,
        String displayName,
        String description,
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