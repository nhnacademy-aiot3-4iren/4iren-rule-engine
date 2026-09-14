package com.nhnacademy.ruleengine.domain.nodeconfig.controller.doc;

import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidateRequest;
import com.nhnacademy.ruleengine.domain.nodeconfig.dto.NodeConfigValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "노드 설정 검증 API", description = "플로우 노드 타입별 설정값을 검증하는 API")
public interface NodeConfigControllerDoc {

    @Operation(
            summary = "노드 설정 검증",
            description = "강의실 센서 구성과 노드 타입에 맞춰 노드 설정값의 유효성을 검증한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "노드 설정 검증 완료"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패 또는 잘못된 노드 설정"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "지원하지 않는 노드 타입")
    })
    @PostMapping("/validate-config")
    ResponseEntity<NodeConfigValidationResponse> validateNodeConfig(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @RequestBody @Valid NodeConfigValidateRequest request
    );
}
