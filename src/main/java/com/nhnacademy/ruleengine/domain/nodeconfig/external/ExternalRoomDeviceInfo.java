package com.nhnacademy.ruleengine.domain.nodeconfig.external;

import java.util.Map;

public record ExternalRoomDeviceInfo(
        Long roomId,
        String devEui,
        String deviceName,
        Map<String, String> measurement//<센서 타입, 단위>
) {
}
