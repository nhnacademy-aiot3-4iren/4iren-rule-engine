package com.nhnacademy.ruleengine.common.advice;

import com.nhnacademy.ruleengine.common.exception.ErrorCode;

import java.util.List;

//
public record ValidationErrorResponse(
        String code,
        String message,
        List<ValidationError> errors
) {
    public static ValidationErrorResponse from(ErrorCode errorCode, List<ValidationError> errors) {
        return new ValidationErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );
    }

    public record ValidationError(
            Long nodeId,//특정 노드와 관련된 노드일결우 해당 노드 ID
            String field,//문제가 발생한 필드 명 ex.nodeConfig.nodrType
            String message//해당 오류 설명
    ) {
        public static List<ValidationError> ofList(List<String> messages) {
            return messages.stream()
                    .map(m -> new ValidationError(null, null, m))
                    .toList();
        }

        public static ValidationError of(String field, String message) {
            return new ValidationError(null, field, message);
        }

        public static ValidationError of(Long nodeId, String field, String message) {
            return new ValidationError(nodeId, field, message);
        }
    }
}
