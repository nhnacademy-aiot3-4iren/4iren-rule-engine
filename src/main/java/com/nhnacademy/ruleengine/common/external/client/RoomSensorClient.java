package com.nhnacademy.ruleengine.common.external.client;

import com.nhnacademy.ruleengine.common.config.FeignUserHeaderInterceptorConfig;
import com.nhnacademy.ruleengine.common.external.dto.RoomDeviceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
    name = "4iren-processing",
    contextId = "roomSensorClient",
    path = "/api/processing",
        configuration = FeignUserHeaderInterceptorConfig.class//유저 정보 헤더 전달
)
public interface RoomSensorClient {

    @GetMapping("/sensors")
    List<RoomDeviceInfo> getRoomDevices(@RequestParam("roomId") Long roomId);


}
