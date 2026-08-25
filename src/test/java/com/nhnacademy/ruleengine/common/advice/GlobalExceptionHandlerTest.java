package com.nhnacademy.ruleengine.common.advice;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import com.nhnacademy.ruleengine.common.exception.invalid.InvalidNodeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessException 발생 시 해당 에러 코드의 상태 코드와 메시지를 반환")
    void handleBusinessException_ReturnsErrorResponse() {
        BusinessException exception = new InvalidNodeException();

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_NODE.getCode());
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.INVALID_NODE.getMessage());
    }

    @Test
    @DisplayName("FlowValidationFailed 발생 시 400 상태 코드와 검증 에러 리스트를 반환")
    void handleValidationError_ReturnsValidationError() {
        List<String> validationErrors = List.of("Node count is invalid", "Cycle detected");
        FlowValidationFailed exception = new FlowValidationFailed(validationErrors);

        ResponseEntity<ValidationError> response = exceptionHandler.handleValidationError(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(exception.getMessage());
        assertThat(response.getBody().errors()).containsExactlyElementsOf(validationErrors);
    }
}