package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;

import java.util.List;

public record TemplateResponse(
        Long templateId,
        String templateName,
        String description,
        List<SensorType> sensorTypes
) {
    public static TemplateResponse of(
            Long templateId,
            String templateName,
            String description,
            List<SensorType> sensorTypes
    ){
        return new TemplateResponse(templateId, templateName, description, sensorTypes);
    }
}
