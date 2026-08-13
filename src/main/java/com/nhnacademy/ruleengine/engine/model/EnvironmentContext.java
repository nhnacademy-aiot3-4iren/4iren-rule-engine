package com.nhnacademy.ruleengine.engine.model;

import java.time.Instant;
import java.util.List;

public record EnvironmentContext(
        Long roomId,
        List<MetricInfo> metrics,
        Instant updatedAt
) {
    public record MetricInfo(
            String metric,
            Double value,
            String devEui,
            Instant updatedAt
    ) {}
}