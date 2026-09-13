package com.nhnacademy.ruleengine.domain.flow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플로우 목록 조회 응답")
public record FlowListResponse(

        @Schema(description = "플로우 요약 목록")
        List<FlowResponse> flowResponseList
) {
    public static FlowListResponse of(
            List<FlowResponse> flowResponseList
    ){
        return new FlowListResponse(flowResponseList);
    }
}
