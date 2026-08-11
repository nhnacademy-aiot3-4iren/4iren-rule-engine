package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record RoomTemplateListResponse(
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
