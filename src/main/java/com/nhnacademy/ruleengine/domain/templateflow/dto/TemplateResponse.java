package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record TemplateResponse(
        Long templateId,

        String templateName,

        String description,

        List<MeasurementType> measurementTypes
) {
    public static TemplateResponse of(Flow flow, List<MeasurementType> measurementTypes){
        return TemplateResponse.builder()
                .templateId(flow.getId())
                .templateName(flow.getFlowName())
                .description(flow.getDescription())
                .measurementTypes(measurementTypes).build();
    }

    public static List<TemplateResponse> fromList(
            List<Flow> flowTemplates, Map<Long, List<MeasurementType>> measurementTypesByFlowId
    ){
        return flowTemplates.stream()
                .map(f ->{
                    List<MeasurementType> measurementTypeList = measurementTypesByFlowId.get(f.getId());

                    return TemplateResponse.builder()
                            .templateId(f.getId())
                            .templateName(f.getFlowName())
                            .description(f.getDescription())
                            .measurementTypes(measurementTypeList)
                            .build();
                }).toList();
    }

}