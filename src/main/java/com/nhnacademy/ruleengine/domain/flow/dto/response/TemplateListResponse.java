package com.nhnacademy.ruleengine.domain.flow.dto.response;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TemplateListResponse(
        @NotNull
        List<TemplateResponse> templateResponseList
) {
    static TemplateListResponse of(List<TemplateResponse> templateResponseList){
        return new TemplateListResponse(templateResponseList);
    }
}
