package com.nhnacademy.ruleengine.engine.model;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;

import java.time.Instant;
import java.util.List;

public record SensorPayload(
        DeviceIdentity device,
        List<SensorData> sensorDataList,
        Instant measuredAt
) {
    public record DeviceIdentity(
            String applicationId,
            String applicationName,
            String deviceProfileId,
            String deviceName,
            String devEui,
            Long roomId,
            String point
    ) {}

    public record SensorData(
            String category,
            MeasurementType measurement,
            Double value
    ) {}
}