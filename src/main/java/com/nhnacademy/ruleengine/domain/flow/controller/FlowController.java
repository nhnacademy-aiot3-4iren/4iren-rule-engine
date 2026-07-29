package com.nhnacademy.ruleengine.domain.flow.controller;


import com.nhnacademy.ruleengine.domain.flow.dto.flow.request.FlowCreateRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.flow.request.FlowUpdateRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.flow.response.*;
import com.nhnacademy.ruleengine.domain.flow.service.FlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/{room_id}/flows")
@RequiredArgsConstructor
public class FlowController {

    private final FlowService flowService;

    //플로우 목록 조회
    @GetMapping
    public ResponseEntity<FlowListResponse> getFlowList(@PathVariable("room_id") Long roomId) {
        FlowListResponse response = flowService.getFlowList(roomId);
        return ResponseEntity.ok(response);
    }

    //플로우 단건(상세) 조회
    @GetMapping("/{flow_id}")
    public ResponseEntity<FlowDetailResponse> getFlowDetail(
            @PathVariable("room_id") Long roomId,
            @PathVariable("flow_id") Long flowId) {
        FlowDetailResponse response = flowService.getFlowDetail(roomId, flowId);
        return ResponseEntity.ok(response);
    }

    //강의실 별 템플릿 플로우 제안 목록
    @GetMapping("/templates")
    public ResponseEntity<TemplateListResponse> getFlowTemplateList(@PathVariable("room_id") Long roomId) {
        TemplateListResponse response = flowService.getFlowTemplateList(roomId);
        return ResponseEntity.ok(response);
    }

    //템플릿 플로우 상세 조회 및 플로우 생성 폼 화면
    @GetMapping("/templates/{template_id}")
    public ResponseEntity<TemplateDetailResponse> getTemplateFlowDetail(
            @PathVariable("room_id") Long roomId,
            @PathVariable("template_id") Long templateId) {
        TemplateDetailResponse response = flowService.getTemplateFlowDetail(roomId, templateId);
        return ResponseEntity.ok(response);
    }

    //플로우 생성
    @PostMapping
    public ResponseEntity<FlowCreateResponse> createFlow(
            @PathVariable("room_id") Long roomId,
            @Valid @RequestBody FlowCreateRequest request) {
        FlowCreateResponse response = flowService.createFlow(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //템플릿으로 플로우 생성
    @PostMapping("/templates/{template_id}")
    public ResponseEntity<FlowCreateResponse> createFlowFromTemplate(
            @PathVariable("room_id") Long roomId,
            @PathVariable("template_id") Long templateId,
            @Valid @RequestBody FlowCreateRequest request) {
        FlowCreateResponse response = flowService.createFlowFromTemplate(roomId, templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //플로우 수정
    @PutMapping("/{flow_id}")
    public ResponseEntity<Void> updateFlow(
            @PathVariable("room_id") Long roomId,
            @PathVariable("flow_id") Long flowId,
            @Valid @RequestBody FlowUpdateRequest request) {
        flowService.updateFlow(roomId, flowId, request);
        return ResponseEntity.ok().build();
    }

    // 플로우 삭제
    @DeleteMapping("/{flow_id}")
    public ResponseEntity<Void> deleteFlow(
            @PathVariable("room_id") Long roomId,
            @PathVariable("flow_id") Long flowId) {
        flowService.deleteFlow(roomId, flowId);
        return ResponseEntity.noContent().build();
    }
}
