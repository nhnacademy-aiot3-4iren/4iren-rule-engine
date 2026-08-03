package com.nhnacademy.ruleengine.domain.nodeconfig.external;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.ExternalRoomDeviceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "4iren-processing")
public interface RoomDeviceClient {

    @GetMapping("/api/sensors")
    List<ExternalRoomDeviceInfo> getRoomDevices(@RequestParam("roomId") Long roomId);
}
