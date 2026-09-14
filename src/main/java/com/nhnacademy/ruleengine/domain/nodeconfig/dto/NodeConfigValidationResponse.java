package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "노드 설정 검증 응답")
public record NodeConfigValidationResponse(
        @Schema(description = "검증 성공 여부", example = "true")
        boolean valid,
        @Schema(description = "검증 결과 메시지", example = "노드 설정이 유효합니다.")
        String message,
        @Schema(description = "검증 실패 상세 목록")
        List<NodeConfigError> errors
) {
    @Schema(description = "노드 설정 검증 실패 상세")
    public record NodeConfigError(
            @Schema(description = "문제가 발생한 필드", example = "threshold")
            String field,
            @Schema(description = "검증 실패 메시지", example = "임계값은 필수입니다.")
            String message
    ){
        public static NodeConfigError of( String field, String message){
            return new NodeConfigError(field, message);
        }
    }
    public static NodeConfigValidationResponse success() {
        return new NodeConfigValidationResponse(true, "노드 설정이 유효합니다.",List.of());
    }

    public static NodeConfigValidationResponse failure(List<NodeConfigError> errors) {
        return new NodeConfigValidationResponse(false, "노드 설정을 확인해주세요.",errors);
    }
}
