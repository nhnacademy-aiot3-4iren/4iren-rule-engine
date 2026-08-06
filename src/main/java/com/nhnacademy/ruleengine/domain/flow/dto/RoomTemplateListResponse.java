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
            List<Flow> templateFlow,
            Map<Long, List<MeasurementType>> sensorTypes
    ){
        List<RoomTemplateResponse> roomTemplateResponses =  RoomTemplateResponse.fromList(templateFlow, sensorTypes);
        return new RoomTemplateListResponse(roomTemplateResponses);
    }
}
