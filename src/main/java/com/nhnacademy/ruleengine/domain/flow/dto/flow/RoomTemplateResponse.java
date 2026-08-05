package com.nhnacademy.ruleengine.domain.flow.dto.flow;

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

        List<MeasurementType> measurementTypes
) {
    public static RoomTemplateResponse from(
            Flow flowTemplate,   List<MeasurementType> measurementType
    ){
        return RoomTemplateResponse.builder()
                .templateId(flowTemplate.getId())
                .templateName(flowTemplate.getFlowName())
                .description(flowTemplate.getDescription())
                .measurementTypes(measurementType).build();
    }

    public static List<RoomTemplateResponse> fromList(
        List<Flow> flowTemplates, Map<Long, List<MeasurementType>> sensorTypesByFlowId
    ){
        return flowTemplates.stream()
                .map(f ->{
                    List<MeasurementType> measurementTypeList = sensorTypesByFlowId.get(f.getId());

                    return RoomTemplateResponse.builder()
                            .templateId(f.getId())
                            .templateName(f.getFlowName())
                            .description(f.getDescription())
                            .measurementTypes(measurementTypeList)
                            .build();
                }).toList();
    }
}
