package com.nhnacademy.ruleengine.domain.flow.dto.flow;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record TemplateListResponse(
        List<TemplateResponse> templateResponseList
) {
    public static TemplateListResponse from(
            List<Flow> templateFlow,
            Map<Long, List<SensorType>> sensorTypes
    ){
        List<TemplateResponse> templateResponses =  TemplateResponse.fromList(templateFlow, sensorTypes);
        return new TemplateListResponse(templateResponses);
    }

}
