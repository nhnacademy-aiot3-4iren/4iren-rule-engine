package com.nhnacademy.ruleengine.domain.flow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
@Builder
@Schema(description = "플로우 빌드 폼 응답")
public record FlowBuildFormResponse(
        @Schema(description = "강의실 ID", example = "1")
        Long roomId,
        @Schema(description = "강의실에서 사용할 수 있는 센서 측정 메타 정보 목록")
        List<SensorMetaInfo> sensorMetaInfoList
) {
    public static FlowBuildFormResponse of(Long roomId, List<SensorMetaInfo> sensorMetaInfoList) {
        return FlowBuildFormResponse.builder().roomId(roomId).sensorMetaInfoList(sensorMetaInfoList).build();
    }

}
