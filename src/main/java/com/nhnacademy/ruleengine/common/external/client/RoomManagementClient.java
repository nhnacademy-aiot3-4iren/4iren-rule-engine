package com.nhnacademy.ruleengine.common.external.client;

import com.nhnacademy.ruleengine.common.external.dto.RoomManagementAccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "4iren-core",
        path = "/api/core"
)
public interface RoomManagementClient {

    @GetMapping("/internal/rooms/{room-id}/users/{user-id}/management-access")
    RoomManagementAccessResponse getManagementAccessAllowed(@PathVariable("room-id") Long roomId, @PathVariable("user-id")Long userId );
}
