package com.nhnacademy.ruleengine.domain.flow.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record FlowBuildFormResponse(
        Long roomId,
        List<SensorMetaInfo> sensorMetaInfoList
) {
    public static FlowBuildFormResponse of(Long roomId, List<SensorMetaInfo> sensorMetaInfoList) {
        return FlowBuildFormResponse.builder().roomId(roomId).sensorMetaInfoList(sensorMetaInfoList).build();
    }

}