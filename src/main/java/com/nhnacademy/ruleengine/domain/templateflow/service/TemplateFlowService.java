package com.nhnacademy.ruleengine.domain.templateflow.service;

import com.nhnacademy.ruleengine.domain.templateflow.dto.*;

public interface TemplateFlowService {
    TemplateFlowCreateResponse createTemplatFlow(TemplateFlowCreateRequest request);
    TemplateListResponse getTemplateList();

    TemplateDetailResponse getTemplateDetail(Long templateId);

    void updateTemplate(Long templateId, TemplateFlowUpdateRequest request);

    void deleteTemplate(Long templateId);
}
