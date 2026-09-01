package com.nhnacademy.ruleengine.common.external.dto;

public record MetricCatalogInfo(
        String metricCode,
        String displayName,
        String metricKind,
        String status,
        String description,
        String ucumCode,
        String unitDisplayName,
        String symbol
) {
}
