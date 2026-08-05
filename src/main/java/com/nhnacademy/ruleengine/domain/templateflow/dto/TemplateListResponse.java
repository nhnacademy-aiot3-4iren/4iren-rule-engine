package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;

import java.util.List;

public record TemplateListResponse(
        List<TemplateResponse> templateResponseList
) {
    record TemplateResponse(
            Long templateId,

            String templateName,

            String description,

            List<MeasurementType> measurementTypes
    ) {}

}
