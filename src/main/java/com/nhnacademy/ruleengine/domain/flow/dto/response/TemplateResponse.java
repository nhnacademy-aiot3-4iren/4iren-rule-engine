package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TemplateResponse(
        @NotNull
        Long templateId,
        @NotBlank
        String templateName,
        String description,
        @NotEmpty
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
