package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "템플릿 플로우 목록 조회 응답")
public record TemplateListResponse(
        @Schema(description = "템플릿 플로우 요약 목록")
        List<TemplateResponse> templateResponseList
) {


    public static TemplateListResponse of(
            List<Flow> templateFlow,
            Map<Long, List<MeasurementType>> measurementTypes
    ){
        List<TemplateResponse> templateResponses =  TemplateResponse.fromList(templateFlow, measurementTypes);
        return new TemplateListResponse(templateResponses);
    }
}
