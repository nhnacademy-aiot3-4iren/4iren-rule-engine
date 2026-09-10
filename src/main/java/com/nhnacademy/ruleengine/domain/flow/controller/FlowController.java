package com.nhnacademy.ruleengine.domain.flow.controller;

import com.nhnacademy.ruleengine.domain.flow.controller.doc.FlowControllerDoc;
import com.nhnacademy.ruleengine.domain.flow.dto.*;
import com.nhnacademy.ruleengine.domain.flow.service.FlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rule/rooms/{room-id}")
@RequiredArgsConstructor
public class FlowController implements FlowControllerDoc {

    private final FlowService flowService;

    //플로우 빌드 폼 호출
    @GetMapping("/flows/form")
    public ResponseEntity<FlowBuildFormResponse> buildForm(
            @PathVariable("room-id") Long roomId
    ){
        log.info("플로우 빌드 폼 요청 roomId={}", roomId);
        FlowBuildFormResponse response = flowService.getFlowBuildForm(roomId);

        return ResponseEntity.ok(response);
    }

    //플로우 생성
    @PostMapping("/flows")
    public ResponseEntity<FlowCreateResponse> createFlow(
            @PathVariable("room-id") Long roomId,
            @Valid @RequestBody FlowCreateRequest request) {
        log.info("플로우 생성 요청 roomId={}, flowName={}", roomId, request.flowName());

        FlowCreateResponse response = flowService.createFlow(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //플로우 목록 조회
    @GetMapping("/flows")
    public ResponseEntity<FlowListResponse> getFlowList(@PathVariable("room-id") Long roomId) {
        log.info("플로우 목록 조회 요청 roomId={}", roomId);

        FlowListResponse response = flowService.getFlowList(roomId);
        return ResponseEntity.ok(response);
    }

    //강의실 별 템플릿 플로우 제안 목록
    @GetMapping("/flow-templates")
    public ResponseEntity<RoomTemplateListResponse> getFlowTemplateList(@PathVariable("room-id") Long roomId) {
        log.info("강의실별 템플릿 플로우 제안 목록 요청 roomId={}", roomId);

        RoomTemplateListResponse response = flowService.getFlowTemplateList(roomId);
        return ResponseEntity.ok(response);
    }

    //플로우 단건(상세) 조회 및 수정 폼
    @GetMapping("/flows/{flow-id}")
    public ResponseEntity<FlowDetailResponse> getFlowDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId) {
        log.info("플로우 상세 조회 요청 roomId={}, flowId={}", roomId, flowId);

        FlowDetailResponse response = flowService.getFlowDetail(roomId, flowId);
        return ResponseEntity.ok(response);
    }


    //추천 템플릿 플로우 상세 조회 및 플로우 생성 폼 화면(강의실별 플로우 관리자 전용)
    @GetMapping("/flow-templates/{template-id}")
    public ResponseEntity<RoomTemplateDetailResponse> getTemplateFlowDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("template-id") Long templateId)
    {
        log.info("추천 템플릿 플로우 상세 조회 요청 roomId={}, templateId={}", roomId, templateId);

        RoomTemplateDetailResponse response = flowService.getTemplateFlowDetail(roomId,templateId);
        return ResponseEntity.ok(response);
    }

    //플로우 수정
    @PutMapping("/flows/{flow-id}")
    public ResponseEntity<Void> updateFlow(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @Valid @RequestBody FlowUpdateRequest request) {
        log.info("플로우 수정 요청 roomId={}, flowId={}, flowName={}", roomId, flowId, request.flowName());

        flowService.updateFlow(roomId, flowId, request);
        return ResponseEntity.noContent().build();
    }

    //플로우 삭제
    @DeleteMapping("/flows/{flow-id}")
    public ResponseEntity<Void> deleteFlow(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId) {
        log.info("플로우 삭제 요청 roomId={}, flowId={}", roomId, flowId);
        flowService.deleteFlow(roomId, flowId);

        return ResponseEntity.noContent().build();
    }

    //플로우 활성화/비활성화 설정
    @PatchMapping("/flows/{flow-id}/active")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @RequestBody  @Valid UpdateFlowStatusRequest request
    ){
        log.info("플로우 활성 상태 변경 요청 roomId={}, flowId={}, isActive={}", roomId, flowId, request.isActive());
        flowService.updateStatus(roomId, flowId, request);

        return ResponseEntity.noContent().build();
    }


}
