package com.nhnacademy.ruleengine.domain.flow.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record SensorMetaResponse(
        Long roomId,
        List<SensorMetaInfo> sensorMetaInfoList
) {
    public static SensorMetaResponse of(Long roomId, List<SensorMetaInfo> sensorMetaInfoList) {
        return SensorMetaResponse.builder().roomId(roomId).sensorMetaInfoList(sensorMetaInfoList).build();
    }

}