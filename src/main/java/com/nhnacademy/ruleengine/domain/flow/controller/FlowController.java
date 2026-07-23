package com.nhnacademy.ruleengine.domain.flow.controller;

import com.nhnacademy.ruleengine.domain.flow.dto.request.FlowCreateRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.request.FlowUpdateRequest;
import com.nhnacademy.ruleengine.domain.flow.dto.response.*;
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

    /**
     * [플로우 목록 조회]
     * GET /api/rooms/{room_id}/flows
     */
    @GetMapping
    public ResponseEntity<FlowListResponse> getFlowList(@PathVariable("roomId") Long roomId) {
        FlowListResponse response = flowService.getFlowList(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * [플로우 단건(상세) 조회]
     * GET /api/rooms/{room_id}/flows/{flow_id}
     */
    @GetMapping("/{flowId}")
    public ResponseEntity<FlowDetailResponse> getFlowDetail(
            @PathVariable("roomId") Long roomId,
            @PathVariable("flowId") Long flowId) {
        FlowDetailResponse response = flowService.getFlowDetail(roomId, flowId);
        return ResponseEntity.ok(response);
    }

    /**
     * [강의실 별 템플릿 플로우 제안 목록]
     * GET /api/rooms/{room_id}/flows/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<TemplateListResponse> getFlowTemplateList(@PathVariable("roomId") Long roomId) {
        TemplateListResponse response = flowService.getFlowTemplateList(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * [템플릿 플로우 상세 조회 및 플로우 생성 폼 화면]
     * GET /api/rooms/{room_id}/flows/templates/{template_id}
     */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<TemplateDetailResponse> getTemplateFlowDetail(
            @PathVariable("roomId") Long roomId,
            @PathVariable("templateId") Long templateId) {
        TemplateDetailResponse response = flowService.getTemplateFlowDetail(roomId, templateId);
        return ResponseEntity.ok(response);
    }

    /**
     * [플로우 생성]
     * POST /api/rooms/{room_id}/flows
     */
    @PostMapping
    public ResponseEntity<FlowCreateResponse> createFlow(
            @PathVariable("roomId") Long roomId,
            @Valid @RequestBody FlowCreateRequest request) {
        FlowCreateResponse response = flowService.createFlow(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * [템플릿으로 플로우 생성]
     * POST /api/rooms/{room_id}/flows/templates/{template_id}
     */
    @PostMapping("/templates/{templateId}")
    public ResponseEntity<FlowCreateResponse> createFlowFromTemplate(
            @PathVariable("roomId") Long roomId,
            @PathVariable("templateId") Long templateId,
            @Valid @RequestBody FlowCreateRequest request) {
        FlowCreateResponse response = flowService.createFlowFromTemplate(roomId, templateId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * [플로우 수정]
     * PUT /api/rooms/{room_id}/flows/{flow_id}
     */
    @PutMapping("/{flowId}")
    public ResponseEntity<Void> updateFlow(
            @PathVariable("roomId") Long roomId,
            @PathVariable("flowId") Long flowId,
            @Valid @RequestBody FlowUpdateRequest request) {
        flowService.updateFlow(roomId, flowId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * [플로우 삭제]
     * DELETE /api/rooms/{room_id}/flows/{flow_id}
     */
    @DeleteMapping("/{flowId}")
    public ResponseEntity<Void> deleteFlow(
            @PathVariable("roomId") Long roomId,
            @PathVariable("flowId") Long flowId) {
        flowService.deleteFlow(roomId, flowId);
        return ResponseEntity.noContent().build();
    }

}
