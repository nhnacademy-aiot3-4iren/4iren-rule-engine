package com.nhnacademy.ruleengine.domain.flow.controller.doc;

import com.nhnacademy.ruleengine.domain.flow.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "플로우 관리 API", description = "강의실별 관리자 전용 플로우 관리 API. 해당 강의실의 관리 팀에 속한 관리자만 접근할 수 있다.")
public interface FlowControllerDoc {

    @Operation(
            summary = "플로우 빌드 폼 호출",
            description = "강의실의 플로우 생성 화면에서 필요한 센서, 측정 항목, 노드 설정 메타데이터를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플로우 빌드 폼 호출 성공"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음")
    })
    @GetMapping("/flows/form")
    ResponseEntity<FlowBuildFormResponse> buildForm(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId
    );

    @Operation(
            summary = "플로우 생성",
            description = "강의실에 플로우를 생성한다. 노드와 연결 정보가 함께 저장된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "플로우 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "409", description = "강의실 활성 플로우 개수 제한 초과")
    })
    @PostMapping("/flows")
    ResponseEntity<FlowCreateResponse> createFlow(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Valid @RequestBody FlowCreateRequest request);

    @Operation(
            summary = "플로우 목록 조회",
            description = "강의실에 등록된 플로우 목록과 각 플로우의 스케줄 개수를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플로우 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음")
    })
    @GetMapping("/flows")
    ResponseEntity<FlowListResponse> getFlowList(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId
    );

    @Operation(
            summary = "강의실별 추천 템플릿 플로우 목록 조회",
            description = "강의실의 센서 구성에 맞춰 생성 가능한 템플릿 플로우 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 템플릿 플로우 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음")
    })
    @GetMapping("/flow-templates")
    ResponseEntity<RoomTemplateListResponse> getFlowTemplateList(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId
    );

    @Operation(
            summary = "플로우 상세 조회",
            description = "플로우의 기본 정보, 노드, 연결, 스케줄 정보를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플로우 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "템플릿 플로우를 일반 플로우로 조회한 경우"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플로우 없음")
    })
    @GetMapping("/flows/{flow-id}")
    ResponseEntity<FlowDetailResponse> getFlowDetail(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId
    );

    @Operation(
            summary = "추천 템플릿 플로우 상세 조회",
            description = "강의실에 적용 가능한 추천 템플릿 플로우의 상세 정보와 생성 폼 데이터를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 템플릿 플로우 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "일반 플로우를 템플릿 플로우로 조회한 경우"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "템플릿 플로우 없음")
    })
    @GetMapping("/flow-templates/{template-id}")
    ResponseEntity<RoomTemplateDetailResponse> getTemplateFlowDetail(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "템플릿 플로우 ID", example = "1", required = true)
            @PathVariable("template-id") Long templateId
    );

    @Operation(
            summary = "플로우 수정",
            description = "플로우의 이름, 노드, 연결 정보를 수정한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "플로우 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "404", description = "플로우 없음"),
            @ApiResponse(responseCode = "409", description = "강의실 활성 플로우 개수 제한 초과")
    })
    @PutMapping("/flows/{flow-id}")
    ResponseEntity<Void> updateFlow(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId,
            @Valid @RequestBody FlowUpdateRequest request
    );

    @Operation(
            summary = "플로우 삭제",
            description = "강의실에 등록된 플로우를 삭제한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "플로우 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "템플릿 플로우 삭제 요청"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락 또는 플로우 접근 권한 없음"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음")
    })
    @DeleteMapping("/flows/{flow-id}")
    ResponseEntity<Void> deleteFlow(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId
    );

    @Operation(
            summary = "플로우 활성화 상태 변경",
            description = "플로우의 활성화 여부를 변경한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "활성화 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 헤더 누락 또는 플로우 접근 권한 없음"),
            @ApiResponse(responseCode = "403", description = "강의실 관리 권한 없음"),
            @ApiResponse(responseCode = "409", description = "강의실 활성 플로우 개수 제한 초과")
    })
    @PatchMapping("/flows/{flow-id}/active")
    ResponseEntity<Void> updateStatus(
            @Parameter(description = "강의실 ID", example = "1", required = true)
            @PathVariable("room-id") Long roomId,
            @Parameter(description = "플로우 ID", example = "1", required = true)
            @PathVariable("flow-id") Long flowId,
            @RequestBody  @Valid UpdateFlowStatusRequest request
    );
}
