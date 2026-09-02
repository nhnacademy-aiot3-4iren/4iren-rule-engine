package com.nhnacademy.ruleengine.domain.nodeconfig.dto;

import java.util.List;

public record NodeConfigValidationResponse(
        boolean valid,
        String message,
        List<NodeConfigError> errors
) {
    public record NodeConfigError(
            String field,
            String detailMessage
    ){
        public static NodeConfigError of( String field, String detailMessage){
            return new NodeConfigError(field, detailMessage);
        }
    }
    public static NodeConfigValidationResponse success() {
        return new NodeConfigValidationResponse(true, "노드 설정이 유효합니다.",List.of());
    }

    public static NodeConfigValidationResponse failure(List<NodeConfigError> errors) {
        return new NodeConfigValidationResponse(false, "노드 설정을 확인해주세요.",errors);
    }
}