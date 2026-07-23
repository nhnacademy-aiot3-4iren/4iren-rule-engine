package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowTemplateSensorType;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record TemplateResponse(
        @NotNull
        Long templateId,
        @NotBlank
        String templateName,
        String description,
        @NotEmpty
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
