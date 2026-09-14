package com.nhnacademy.ruleengine.domain.flow.dto;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
@Schema(description = "강의실별 추천 템플릿 플로우 요약 응답")
public record RoomTemplateResponse(
        @Schema(description = "템플릿 플로우 ID", example = "1")
        Long templateId,

        @Schema(description = "템플릿 플로우 이름", example = "온도 알림 템플릿")
        String templateName,

        @Schema(description = "템플릿 플로우 설명", example = "온도 조건을 만족하면 알림을 발송하는 템플릿")
        String description,

        @Schema(description = "템플릿에서 사용하는 측정 타입 목록", example = "TEMPERATURE")
        List<String> measurementTypes
) {
    public static RoomTemplateResponse from(
            Flow flowTemplate, List<MeasurementType> measurementTypes
    ){
        return RoomTemplateResponse.builder()
                .templateId(flowTemplate.getId())
                .templateName(flowTemplate.getFlowName())
                .description(flowTemplate.getDescription())
                .measurementTypes(MeasurementType.toNames(measurementTypes))
                .build();
    }

    public static List<RoomTemplateResponse> fromList(
        List<Flow> flowTemplates, Map<Long, List<MeasurementType>> measurementTypesByFlowId
    ){
        return flowTemplates.stream()
                .map(f ->{
                    List<MeasurementType> measurementTypeList = measurementTypesByFlowId.getOrDefault(f.getId(), List.of());

                    return RoomTemplateResponse.builder()
                            .templateId(f.getId())
                            .templateName(f.getFlowName())
                            .description(f.getDescription())
                            .measurementTypes(MeasurementType.toNames(measurementTypeList))
                            .build();
                }).toList();
    }


}
