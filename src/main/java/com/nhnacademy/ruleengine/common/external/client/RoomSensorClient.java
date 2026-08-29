package com.nhnacademy.ruleengine.common.external.client;

import com.nhnacademy.ruleengine.common.external.dto.MetricCatalogInfo;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
    name = "4iren-processing",
    path = "/api/processing"
)
public interface RoomSensorClient {

    @GetMapping("/sensors")
    List<RoomDeviceInfo> getRoomDevices(@RequestParam("room-id") Long roomId);

    @GetMapping("/internal/metric-catalog")
    List<MetricCatalogInfo> getMetricCatalog();
}
