package com.nhnacademy.ruleengine.domain.flowschedule.controller;

import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateRequest;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleListResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.service.FlowScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/{room_id}/flow/{flow_id}/schedules")
@RequiredArgsConstructor
public class FlowScheduleController {
    private final FlowScheduleService flowScheduleService;

    //플로우 스케줄 생성
    @PostMapping
    public ResponseEntity<FlowScheduleCreateResponse> create(
            @PathVariable("room_id") Long roomId,
            @PathVariable("flow_id") Long flowId,
            @RequestBody @Valid FlowScheduleCreateRequest request
            ){

        return ResponseEntity.status(HttpStatus.CREATED).body(flowScheduleService.createFlowSchedule(roomId,flowId, request));
    }


    //특정 플로우의 스케줄 목록 조회
    @GetMapping
    public ResponseEntity<FlowScheduleListResponse> getList(
            @PathVariable("room_id") Long roomId,
            @PathVariable("flow_id") Long flowId
    ){
        return ResponseEntity.ok(flowScheduleService.getFlowScheduleList( flowId));
    }

    //특정 플로우 스케줄 상세 조회
    @GetMapping("/{schedule_id}")
    public ResponseEntity<FlowScheduleResponse> getDetail(
            @PathVariable("room_id") Long roomId,
            @PathVariable("flow_id") Long flowId,
            @PathVariable("schedule_id") Long scheduleId
    ){
        return ResponseEntity.ok(flowScheduleService.getFlowScheduleDetail(scheduleId));
    }

    @DeleteMapping("/{schedule_id}")
    public ResponseEntity<Void> delete(
            @PathVariable("room_id") Long roomId,
            @PathVariable("flow_id") Long flowId,
            @PathVariable("schedule_id") Long scheduleId
    ){
        flowScheduleService.deleteFlowSchedule( scheduleId);
        return ResponseEntity.noContent().build();
    }

}
