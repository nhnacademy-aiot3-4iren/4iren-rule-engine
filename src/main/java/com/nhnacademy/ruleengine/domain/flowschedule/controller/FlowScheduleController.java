package com.nhnacademy.ruleengine.domain.flowschedule.controller;

import com.nhnacademy.ruleengine.domain.flowschedule.controller.doc.FlowScheduleControllerDoc;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateRequest;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleListResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.service.FlowScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rule/rooms/{room-id}/flows/{flow-id}/schedules")
@RequiredArgsConstructor
@Slf4j
public class FlowScheduleController implements FlowScheduleControllerDoc {
    private final FlowScheduleService flowScheduleService;

    //플로우 스케줄 생성
    @PostMapping
    public ResponseEntity<FlowScheduleCreateResponse> create(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @RequestBody @Valid FlowScheduleCreateRequest request
            ){
        log.info("플로우 스케줄 생성 요청 roomId={}, flowId={}, dayOfWeek={}", roomId, flowId, request.dayOfWeek());
        return ResponseEntity.status(HttpStatus.CREATED).body(flowScheduleService.createFlowSchedule(roomId,flowId, request));
    }

    //특정 플로우의 스케줄 목록 조회
    @GetMapping
    public ResponseEntity<FlowScheduleListResponse> getList(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId
    ){
        log.info("플로우 스케줄 목록 조회 요청 roomId={}, flowId={}", roomId, flowId);
        return ResponseEntity.ok(flowScheduleService.getFlowScheduleList( flowId,  roomId));
    }

    //특정 플로우 스케줄 상세 조회
    @GetMapping("/{schedule-id}")
    public ResponseEntity<FlowScheduleResponse> getDetail(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @PathVariable("schedule-id") Long scheduleId
    ){
        log.info("플로우 스케줄 상세 조회 요청 roomId={}, flowId={}, scheduleId={}", roomId, flowId, scheduleId);
        return ResponseEntity.ok(flowScheduleService.getFlowScheduleDetail(roomId, flowId, scheduleId));
    }

    @DeleteMapping("/{schedule-id}")
    public ResponseEntity<Void> delete(
            @PathVariable("room-id") Long roomId,
            @PathVariable("flow-id") Long flowId,
            @PathVariable("schedule-id") Long scheduleId
    ){
        log.info("플로우 스케줄 삭제 요청 roomId={}, flowId={}, scheduleId={}", roomId, flowId, scheduleId);
        flowScheduleService.deleteFlowSchedule( roomId, flowId,scheduleId);
        return ResponseEntity.noContent().build();
    }

}
