package com.nhnacademy.ruleengine.domain.flow.dto.response;

import java.util.List;

public record TemplateListResponse(
        List<TemplateResponse> templateResponseList
) {
    static TemplateListResponse of(List<TemplateResponse> templateResponseList){
        return new TemplateListResponse(templateResponseList);
    }
}
