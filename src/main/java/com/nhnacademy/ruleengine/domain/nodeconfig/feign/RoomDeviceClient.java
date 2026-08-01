package com.nhnacademy.ruleengine.domain.nodeconfig.feign;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.external.ExternalRoomDeviceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "4iren-processing")
public interface RoomDeviceClient {
    @GetMapping("/api/")
    List<ExternalRoomDeviceInfo> getRoomDevices(@RequestParam("roomId") Long roomId);
}
