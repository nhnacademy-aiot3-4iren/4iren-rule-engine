package com.nhnacademy.ruleengine.domain.templateflow.controller;

import com.nhnacademy.ruleengine.domain.templateflow.dto.*;
import com.nhnacademy.ruleengine.domain.templateflow.service.TemplateFlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(" /api/templates/")
public class TemplateFlowController {

    TemplateFlowService templateFlowService;


    // [템플릿 플로우 생성]
    @PostMapping
    public ResponseEntity<TemplateCreateResponse> createTemplateFlow(
            @Valid @RequestBody TemplateFlowCreateRequest request) {

        TemplateCreateResponse response = templateFlowService.createTemplatFlow(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // [템플릿 플로우 목록 조회] GET /api/templates/
    @GetMapping
    public ResponseEntity<TemplateListResponse> getTemplateFlowList() {

        TemplateListResponse response = templateFlowService.getTemplateList();

        return ResponseEntity.ok().body(response);
    }

    // [템플릿 플로우 상세 조회 및 템플릿 플로우 수정 폼 화면] GET /api/templates/{template_id}
    @GetMapping("/{template-id}")
    public ResponseEntity<TemplateDetailResponse> getTemplateFlowDetail(
            @PathVariable("template-id") Long templateId) {

        TemplateDetailResponse response = templateFlowService.getTemplateDetail(templateId);

        return ResponseEntity.ok().body(response);
    }

    // [템플릿 플로우 수정] POST /api/templates/{template_id}
    @PostMapping("/{template-id}")
    public ResponseEntity<Void> updateTemplateFlow(
            @PathVariable("template-id") Long templateId,
            @Valid @RequestBody TemplateFlowUpdateRequest request) {

        templateFlowService.updateTemplate(templateId, request);

        return ResponseEntity.noContent().build();
    }

    // [템플릿 플로우 삭제] DELETE /api/templates/{template-id}
    @DeleteMapping("/{template-id}")
    public ResponseEntity<Void> deleteTemplateFlow(
            @PathVariable("template-id") Long templateId) {

        templateFlowService.deleteTemplate(templateId);

        return ResponseEntity.noContent().build();
    }
}

