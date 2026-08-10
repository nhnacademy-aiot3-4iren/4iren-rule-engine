package com.nhnacademy.ruleengine.domain.flowschedule.service;


import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateRequest;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleListResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleResponse;

public interface FlowScheduleService {
    //플로우 스케줄 생성
    FlowScheduleCreateResponse createFlowSchedule(Long roomId, Long flowId, FlowScheduleCreateRequest request);
    //특정 플로우의 스케줄 목록 조회
    FlowScheduleListResponse getFlowScheduleList( Long flowId, Long roomId);

    //특정 플로우의 특정 스케줄 조회
    FlowScheduleResponse getFlowScheduleDetail(Long roomId, Long flowId,Long scheduleId);

    //플로우 스케줄 삭제
    void deleteFlowSchedule( Long roomId, Long flowId,Long scheduleId);
}
