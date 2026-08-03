package com.nhnacademy.ruleengine.domain.flow.dto.flow;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.SensorType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record TemplateResponse(
        Long templateId,

        String templateName,

        String description,

        List<SensorType> sensorTypes
) {
    public static TemplateResponse from(
            Flow flowTemplate,   List<SensorType> sensorType
    ){
        return TemplateResponse.builder()
                .templateId(flowTemplate.getId())
                .templateName(flowTemplate.getFlowName())
                .description(flowTemplate.getDescription())
                .sensorTypes(sensorType).build();
    }

    public static List<TemplateResponse> fromList(
        List<Flow> flowTemplates, Map<Long, List<SensorType>> sensorTypesByFlowId
    ){
        return flowTemplates.stream()
                .map(f ->{
                    List<SensorType> sensorTypeList = sensorTypesByFlowId.get(f.getId());

                    return TemplateResponse.builder()
                            .templateId(f.getId())
                            .templateName(f.getFlowName())
                            .description(f.getDescription())
                            .sensorTypes(sensorTypeList)
                            .build();
                }).toList();
    }
}
