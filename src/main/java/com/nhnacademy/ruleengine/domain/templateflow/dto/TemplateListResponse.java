package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;

import java.util.List;
import java.util.Map;

public record TemplateListResponse(
        List<TemplateResponse> templateResponseList
) {


    public static TemplateListResponse of(
            List<Flow> templateFlow,
            Map<Long, List<MeasurementType>> measurementTypes
    ){
        List<TemplateResponse> TemplateResponses =  TemplateResponse.fromList(templateFlow, measurementTypes);
        return new TemplateListResponse(TemplateResponses);
    }
}
