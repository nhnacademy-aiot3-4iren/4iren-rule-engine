package com.nhnacademy.ruleengine.domain.flow.controller;

import com.nhnacademy.ruleengine.domain.flow.dto.flow.*;
import com.nhnacademy.ruleengine.domain.flow.service.FlowService;
import com.nhnacademy.ruleengine.domain.flow.dto.flow.RoomTemplateDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/{room-id}/flows")
@RequiredArgsConstructor
public class FlowController {

    private final FlowService flowService;

    //플로우 생성
    @PostMapping
    public ResponseEntity<FlowCreateResponse> createFlow(
            @PathVariable("room-id") Long roomId,
            @Valid @RequestBody FlowCreateRequest request) {
        FlowCreateResponse response = flowService.createFlow(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    //템플릿으로 플로우 생성 -> 일반적인 플로우 생성으로 api 통일
//    @PostMapping("/templates/{template-id}")
//    public ResponseEntity<FlowCreateResponse> createFlowFromTemplate(
//            @PathVariable("room-id") Long roomId,
//            @PathVariable("template-id") Long templateId,
//            @Valid @RequestBody FlowCreateRequest request) {
//        FlowCreateResponse response = flowService.createFlowFromTemplate(roomId, templateId, request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    //플로우 목록 조회
    @GetMapping
    public ResponseEntity<FlowListResponse> getFlowList(@PathVariable("room-id") Long roomId) {
        FlowListResponse response = flowService.getFlowList(roomId);
        return ResponseEntity.ok(response);
    }

    //강의실 별 템플릿 플로우 제안 목록
    @GetMapping("/templates")
    public ResponseEntity<RoomTemplateListResponse> getFlowTemplateList(@PathVariable("room-id") Long roomId) {
        RoomTemplateListResponse response = flowService.getFlowTemplateList(roomId);
        return ResponseEntity.ok(response);
    }

    //플로우 단건(상세) 조회 및 수정 폼
    @GetMapping("/{flow-id}")
    public ResponseEntity<FlowDetailResponse> getFlowDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId) {
        FlowDetailResponse response = flowService.getFlowDetail(roomId, flowId);
        return ResponseEntity.ok(response);
    }


    //추천 템플릿 플로우 상세 조회 및 플로우 생성 폼 화면(강의실별 플로우 관리자 전용)
    @GetMapping("/templates/{template-id}")
    public ResponseEntity<RoomTemplateDetailResponse> getTemplateFlowDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("template-id") Long templateId) {
        RoomTemplateDetailResponse response = flowService.getTemplateFlowDetail(roomId, templateId);
        return ResponseEntity.ok(response);
    }

    //플로우 수정
    @PutMapping("/{flow-id}")
    public ResponseEntity<Void> updateFlow(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @Valid @RequestBody FlowUpdateRequest request) {
        flowService.updateFlow(roomId, flowId, request);
        return ResponseEntity.noContent().build();
    }

    //플로우 삭제
    @DeleteMapping("/{flow-id}")
    public ResponseEntity<Void> deleteFlow(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId) {
        flowService.deleteFlow(roomId, flowId);

        return ResponseEntity.noContent().build();
    }

}
