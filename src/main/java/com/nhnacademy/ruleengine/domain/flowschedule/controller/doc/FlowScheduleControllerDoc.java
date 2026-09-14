package com.nhnacademy.ruleengine.domain.flowschedule.controller.doc;

import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateRequest;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleListResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "플로우 스케줄 API", description = "플로우 실행 가능 요일과 시간대를 관리하는 API")
public interface FlowScheduleControllerDoc {

    @Operation(
            summary = "플로우 스케줄 생성",
            description = "특정 플로우에 실행 스케줄을 생성한다. 같은 요일의 시간대가 겹치면 생성할 수 없다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "플로우 스케줄 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플로우 없음")
    })
    @PostMapping
    ResponseEntity<FlowScheduleCreateResponse> create(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId,
            @RequestBody @Valid FlowScheduleCreateRequest request
    );

    @Operation(
            summary = "플로우 스케줄 목록 조회",
            description = "특정 플로우에 등록된 실행 스케줄 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플로우 스케줄 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플로우 없음")
    })
    @GetMapping
    ResponseEntity<FlowScheduleListResponse> getList(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId
    );

    @Operation(
            summary = "플로우 스케줄 상세 조회",
            description = "특정 플로우 스케줄의 요일과 실행 시간대를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플로우 스케줄 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플로우 스케줄 없음")
    })
    @GetMapping("/{schedule-id}")
    ResponseEntity<FlowScheduleResponse> getDetail(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId,
            @Parameter(description = "스케줄 ID", example = "1", required = true)
            @PathVariable("schedule-id") Long scheduleId
    );

    @Operation(
            summary = "플로우 스케줄 삭제",
            description = "특정 플로우 스케줄을 삭제한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "플로우 스케줄 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플로우 스케줄 없음")
    })
    @DeleteMapping("/{schedule-id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId,
            @Parameter(description = "스케줄 ID", example = "1", required = true)
            @PathVariable("schedule-id") Long scheduleId
    );
}
