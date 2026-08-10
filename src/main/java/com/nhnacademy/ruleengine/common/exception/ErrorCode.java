package com.nhnacademy.ruleengine.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    //400 bad request
    INVALID_CONNECTION(HttpStatus.BAD_REQUEST, "INVALID_CONNECTION", "유효하지 않은 Connection입니다."),
    INVALID_FLOW(HttpStatus.BAD_REQUEST, "INVALID_FLOW", "유효하지 않은 Flow입니다."),
    INVALID_FLOW_SCHEDULE(HttpStatus.BAD_REQUEST, "INVALID_FLOW_SCHEDULE", "유효하지 않은 Flow Schedule입니다."),
    INVALID_NODE(HttpStatus.BAD_REQUEST, "INVALID_NODE", "유효하지 않은 Node입니다."),
    INVALID_PAYLOAD(HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD", "유효하지 않은 센서 페이로드입니다."),

    //검증 응답용(ValidationError)
    FLOW_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "FLOW_VALIDATION_FAILED","플로우 무결성 검증에 실패했습니다."),

    //401 unauthorized
    UNAUTHORIZED_FLOW_ACCESS(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_FLOW_ACCESS", "해당 Flow에 접근 권한이 없습니다."),

    //404 not found
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONNECTION_NOT_FOUND", "Connection을 찾을 수 없습니다."),
    FLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOW_NOT_FOUND", "Flow를 찾을 수 없습니다."),
    FLOW_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOW_SCHEDULE_NOT_FOUND", "Flow Schedule을 찾을 수 없습니다."),
    NODE_NOT_FOUND(HttpStatus.NOT_FOUND, "NODE_NOT_FOUND", "Node를 찾을 수 없습니다."),
    MEASUREMENT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEASUREMENT_TYPE_NOT_FOUND", "Measurement Type을 찾을 수 없습니다."),
    FLOW_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND,"FLOW_TEMPLATE_NOT_FOUND", "Flow Template를 찾을 수 없습니다."),
    NODE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "NODE_TYPE_NOT_FOUND", "NodeType을 찾을 수 없습니다."),

    //409 conflict
    CONNECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONNECTION_ALREADY_EXISTS", "이미 존재하는 Connection입니다."),
    FLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "FLOW_ALREADY_EXISTS", "이미 존재하는 Flow입니다."),
    FLOW_SCHEDULE_ALREADY_EXISTS(HttpStatus.CONFLICT, "FLOW_SCHEDULE_ALREADY_EXISTS", "이미 존재하는 Flow Schedule입니다."),
    NODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "NODE_ALREADY_EXISTS", "이미 존재하는 Node입니다.");

    //500

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
