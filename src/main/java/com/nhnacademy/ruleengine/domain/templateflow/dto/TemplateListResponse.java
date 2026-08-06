package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.dto.FlowResponse;
import com.nhnacademy.ruleengine.domain.flow.dto.RoomTemplateListResponse;
import com.nhnacademy.ruleengine.domain.flow.dto.RoomTemplateResponse;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

public record TemplateListResponse(
        List<TemplateResponse> templateResponseList
) {


    public static TemplateListResponse of(
            List<Flow> templateFlow,
            Map<Long, List<MeasurementType>> sensorTypes
    ){
        List<TemplateResponse> TemplateResponses =  TemplateResponse.fromList(templateFlow, sensorTypes);
        return new TemplateListResponse(TemplateResponses);
    }
}
