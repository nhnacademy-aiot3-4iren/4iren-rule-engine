package com.nhnacademy.ruleengine.domain.templateflow.controller.doc;

import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateDetailResponse;
import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateFlowCreateRequest;
import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateFlowCreateResponse;
import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateFlowUpdateRequest;
import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateListResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
@Tag(name = "템플릿 플로우 API", description = "관리자가 공통으로 사용할 템플릿 플로우를 생성, 조회, 수정, 삭제하는 API. 프론트에서 접근 불가, 개발자만 접근 가능함")
public interface TemplateFlowControllerDoc {

    @Operation(
            summary = "템플릿 플로우 생성",
            description = "공통 템플릿 플로우를 생성한다. 노드와 연결 정보가 함께 저장된다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "템플릿 플로우 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패")
    })
    @PostMapping
    ResponseEntity<TemplateFlowCreateResponse> createTemplateFlow(
            @Valid @RequestBody TemplateFlowCreateRequest request
    );

    @Operation(
            summary = "템플릿 플로우 목록 조회",
            description = "등록된 템플릿 플로우 목록을 조회한다."
    )
    @ApiResponse(responseCode = "200", description = "템플릿 플로우 목록 조회 성공")
    @GetMapping
    ResponseEntity<TemplateListResponse> getTemplateFlowList();

    @Operation(
            summary = "템플릿 플로우 상세 조회",
            description = "템플릿 플로우의 기본 정보, 노드, 연결 정보를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "템플릿 플로우 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "템플릿 플로우 없음")
    })
    @GetMapping("/{template-id}")
    ResponseEntity<TemplateDetailResponse> getTemplateFlowDetail(
            @Parameter(description = "템플릿 플로우 ID", example = "1", required = true)
            @PathVariable("template-id") Long templateId
    );

    @Operation(
            summary = "템플릿 플로우 수정",
            description = "템플릿 플로우의 이름, 노드, 연결 정보를 수정한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "템플릿 플로우 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패"),
            @ApiResponse(responseCode = "404", description = "템플릿 플로우 없음")
    })
    @PutMapping("/{template-id}")
    ResponseEntity<Void> updateTemplateFlow(
            @Parameter(description = "템플릿 플로우 ID", example = "1", required = true)
            @PathVariable("template-id") Long templateId,
            @Valid @RequestBody TemplateFlowUpdateRequest request
    );

    @Operation(
            summary = "템플릿 플로우 삭제",
            description = "등록된 템플릿 플로우를 삭제한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "템플릿 플로우 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "템플릿 플로우 없음")
    })
    @DeleteMapping("/{template-id}")
    ResponseEntity<Void> deleteTemplateFlow(
            @Parameter(description = "템플릿 플로우 ID", example = "1", required = true)
            @PathVariable("template-id") Long templateId
    );
}
