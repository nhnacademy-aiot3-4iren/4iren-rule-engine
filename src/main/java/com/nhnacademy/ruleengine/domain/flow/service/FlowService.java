package com.nhnacademy.ruleengine.domain.flow.service;

import com.nhnacademy.ruleengine.domain.flow.dto.flow.*;
import com.nhnacademy.ruleengine.domain.flow.dto.flow.RoomTemplateDetailResponse;

public interface FlowService {
    //플로우 생성
    FlowCreateResponse createFlow(Long roomeId, FlowCreateRequest request);

    //템플릿 기반 플로우 생성
    FlowCreateResponse createFlowFromTemplate(Long roomId, Long templateId, FlowCreateRequest request);

    //플로우 목록 조회
    FlowListResponse getFlowList(Long roomId);

    //플로우 상세 조회
    FlowDetailResponse getFlowDetail(Long roomId, Long flowId);

    //강의실별 템플릿 플로우 제안 목록
    RoomTemplateListResponse getFlowTemplateList(Long roomId);

    //템플릿 기반 플로우 생성 폼 = 템플릿 플로우 상세
    RoomTemplateDetailResponse getTemplateFlowDetail(Long roomId, Long templateFlowId);

    //플로우 수정
    void updateFlow(Long roomId, Long flowId, FlowUpdateRequest request);

    //플로우 삭제
    void deleteFlow(Long roomId, Long flowId);

    //플로우 활성화/비활성화-> 보류
}
