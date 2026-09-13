package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
@Schema(description = "강의실별 추천 템플릿 플로우 목록 응답")
public record RoomTemplateListResponse(
        @Schema(description = "강의실에 적용 가능한 템플릿 플로우 목록")
        List<RoomTemplateResponse> roomTemplateResponseList
) {
    public static RoomTemplateListResponse from(
            List<Flow> templateFlowList,
            Map<Long, List<MeasurementType>> measurementTypes
    ){
        List<RoomTemplateResponse> roomTemplateResponses =  RoomTemplateResponse.fromList(templateFlowList, measurementTypes);
        return new RoomTemplateListResponse(roomTemplateResponses);
    }
}
