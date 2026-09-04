package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record RoomTemplateResponse(
        Long templateId,

        String templateName,

        String description,

        List<String> measurementTypes
) {
    public static RoomTemplateResponse from(
            Flow flowTemplate, List<MeasurementType> measurementTypes
    ){
        return RoomTemplateResponse.builder()
                .templateId(flowTemplate.getId())
                .templateName(flowTemplate.getFlowName())
                .description(flowTemplate.getDescription())
                .measurementTypes(MeasurementType.toNames(measurementTypes))
                .build();
    }

    public static List<RoomTemplateResponse> fromList(
        List<Flow> flowTemplates, Map<Long, List<MeasurementType>> measurementTypesByFlowId
    ){
        return flowTemplates.stream()
                .map(f ->{
                    List<MeasurementType> measurementTypeList = measurementTypesByFlowId
                            .getOrDefault(f.getId(), List.of())
                            .stream()
                            .distinct()
                            .toList();

                    return RoomTemplateResponse.builder()
                            .templateId(f.getId())
                            .templateName(f.getFlowName())
                            .description(f.getDescription())
                            .measurementTypes(MeasurementType.toNames(measurementTypeList))
                            .build();
                }).toList();
    }


}
