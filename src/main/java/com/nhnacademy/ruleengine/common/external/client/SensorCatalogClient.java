package com.nhnacademy.ruleengine.common.external.client;

import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "4iren-processing",
        contextId = "sensorCatalogClient",
        path = "/api/processing"
)
public interface SensorCatalogClient {

    @GetMapping("/internal/metric-catalog")
    List<MetricCatalogInfo> getMetricCatalog();
}