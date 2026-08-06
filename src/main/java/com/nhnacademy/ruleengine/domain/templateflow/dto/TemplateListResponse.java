package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.dto.FlowResponse;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

import java.util.List;

public record TemplateListResponse(
        List<TemplateResponse> templateResponseList
) {
    @Builder
    record TemplateResponse(
            Long templateId,

            String templateName,

            String description,

            List<MeasurementType> measurementTypes
    ) {
        public static List<TemplateResponse> fromList(List<Flow> flowList, List<MeasurementType> measurementTypes){
            return flowList.stream()
                    .map( flow -> TemplateResponse.builder()
                            .templateId(flow.getId()).templateName(flow.getFlowName()).description(flow.getDescription()).measurementTypes(measurementTypes).build()
                    ).toList();
        }
    }
    public static TemplateListResponse of(List<Flow> flowList, List<MeasurementType> measurementTypes){
        return new TemplateListResponse(TemplateResponse.fromList(flowList, measurementTypes));
    }


}
