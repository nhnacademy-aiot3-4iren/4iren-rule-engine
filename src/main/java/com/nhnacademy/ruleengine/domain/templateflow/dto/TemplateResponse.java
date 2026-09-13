package com.nhnacademy.ruleengine.domain.templateflow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
@Schema(description = "템플릿 플로우 요약 응답")
public record TemplateResponse(
        @Schema(description = "템플릿 플로우 ID", example = "1")
        Long templateId,

        @Schema(description = "템플릿 플로우 이름", example = "온도 알림 템플릿")
        String templateName,

        @Schema(description = "템플릿 플로우 설명", example = "온도 조건을 만족하면 알림을 발송하는 템플릿")
        String description,

        @Schema(description = "템플릿에서 사용하는 측정 타입 목록", example = "TEMPERATURE")
        List<MeasurementType> measurementTypes
) {
    public static TemplateResponse of(Flow flow, List<MeasurementType> measurementTypes){
        return TemplateResponse.builder()
                .templateId(flow.getId())
                .templateName(flow.getFlowName())
                .description(flow.getDescription())
                .measurementTypes(measurementTypes).build();
    }

    public static List<TemplateResponse> fromList(
            List<Flow> flowTemplates, Map<Long, List<MeasurementType>> measurementTypesByFlowId
    ){
        return flowTemplates.stream()
                .map(f ->{
                    List<MeasurementType> measurementTypeList = measurementTypesByFlowId.get(f.getId());

                    return TemplateResponse.builder()
                            .templateId(f.getId())
                            .templateName(f.getFlowName())
                            .description(f.getDescription())
                            .measurementTypes(measurementTypeList)
                            .build();
                }).toList();
    }

}
