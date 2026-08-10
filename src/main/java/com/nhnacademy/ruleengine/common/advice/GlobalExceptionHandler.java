package com.nhnacademy.ruleengine.common.advice;

import com.nhnacademy.ruleengine.common.exception.BusinessException;
import com.nhnacademy.ruleengine.common.exception.ErrorCode;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowValidationFailed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity.status(errorCode.getStatus())
                .body(
                        new ErrorResponse(
                        errorCode.getCode(),
                        errorCode.getMessage()
                ));
    }

    //
    @ExceptionHandler(FlowValidationFailed.class)
    public ResponseEntity<ValidationError> handleValidationError(FlowValidationFailed e){


        return ResponseEntity.badRequest()
                .body(
                    new ValidationError(
                            e.getMessage(),
                            e.getErrors())
                );
    }
}
