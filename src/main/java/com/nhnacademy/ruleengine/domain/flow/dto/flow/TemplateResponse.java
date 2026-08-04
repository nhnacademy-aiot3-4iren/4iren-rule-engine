package com.nhnacademy.ruleengine.domain.flow.dto.flow;

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
    public static TemplateResponse from(
            Flow flowTemplate,   List<MeasurementType> measurementType
    ){
        return TemplateResponse.builder()
                .templateId(flowTemplate.getId())
                .templateName(flowTemplate.getFlowName())
                .description(flowTemplate.getDescription())
                .measurementTypes(measurementType).build();
    }

    public static List<TemplateResponse> fromList(
        List<Flow> flowTemplates, Map<Long, List<MeasurementType>> sensorTypesByFlowId
    ){
        return flowTemplates.stream()
                .map(f ->{
                    List<MeasurementType> measurementTypeList = sensorTypesByFlowId.get(f.getId());

                    return TemplateResponse.builder()
                            .templateId(f.getId())
                            .templateName(f.getFlowName())
                            .description(f.getDescription())
                            .measurementTypes(measurementTypeList)
                            .build();
                }).toList();
    }
}
