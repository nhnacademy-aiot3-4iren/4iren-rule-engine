package com.nhnacademy.ruleengine.domain.flowschedule.service;


import com.nhnacademy.ruleengine.common.advice.ValidationErrorResponse;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowScheduleValidationFailed;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowScheduleNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.*;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class FlowScheduleService {
    private final FlowRepository flowRepository;
    private final FlowScheduleRepository flowScheduleRepository;

    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public FlowScheduleCreateResponse createFlowSchedule(Long roomId, Long flowId, FlowScheduleCreateRequest request) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);

        validateCreateRequest(flowId, request);

        FlowSchedule flowSchedule = FlowSchedule.create(flow, request);
        FlowSchedule savedFlowSchedule = flowScheduleRepository.save(flowSchedule);

        return FlowScheduleCreateResponse.of(savedFlowSchedule.getId());
    }

    @Transactional(readOnly = true)
    public FlowScheduleListResponse getFlowScheduleList(Long flowId, Long roomId) {
        if (!flowRepository.existsByIdAndRoomId(flowId, roomId)) {
            throw new FlowNotFoundException();
        }
        List<FlowSchedule> flowScheduleList = flowScheduleRepository.findAllByFlowId(flowId);

        return FlowScheduleListResponse.from(flowId, flowScheduleList);
    }

    @Transactional(readOnly = true)
    public FlowScheduleResponse getFlowScheduleDetail(Long roomId, Long flowId, Long scheduleId) {

        FlowSchedule flowSchedule = flowScheduleRepository.findSchedule(scheduleId, flowId, roomId)
                .orElseThrow(FlowScheduleNotFoundException::new);

        return FlowScheduleResponse.from(flowSchedule);
    }

    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public void deleteFlowSchedule( Long roomId, Long flowId, Long scheduleId) {
        if (!flowScheduleRepository.existsFlowSchedule(scheduleId, flowId, roomId)) {
            throw new FlowScheduleNotFoundException();
        }
        flowScheduleRepository.deleteById(scheduleId);
    }

    //검증 코드
    private void validateCreateRequest(Long flowId, FlowScheduleCreateRequest request){
        List<ValidationErrorResponse.ValidationError> errors = new ArrayList<>();

        boolean validRange = validationTimeRange(request, errors);
        if(validRange){
            validateNoOverlap(flowId, request, errors);
        }
        if (!errors.isEmpty()) {
            throw new FlowScheduleValidationFailed(errors);
        }
    }

    //스케줄 시간대 겹침 여부 검증
    private void validateNoOverlap(Long flowId, FlowScheduleCreateRequest request, List<ValidationErrorResponse.ValidationError> errors) {
        if (request.dayOfWeek() == null || request.startTime() == null || request.endTime() == null) {
            return;
        }

        List<FlowSchedule> schedules = flowScheduleRepository.findAllByFlowIdAndDayOfWeek(flowId, request.dayOfWeek());

        boolean overlapped = schedules.stream()
                .anyMatch(schedule ->
                            request.startTime().isBefore(schedule.getEndTime())
                        && request.endTime().isAfter(schedule.getStartTime())
                );

        if(overlapped){
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "FlowSchedule",
                    "같은 요일에 시간이 겹치는 스케줄이 이미 존재합니다."
            ));
        }


    }

    //시작시간 종료시간 범위 검증
    private boolean validationTimeRange(FlowScheduleCreateRequest request, List<ValidationErrorResponse.ValidationError> errors) {

        if(request.startTime() == null || request.endTime() == null){
            return false;
        }

        if(request.startTime().equals(request.endTime())){
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "FlowSchedule",
                    "시작 시간과 종료 시간은 같을 수 없습니다."
            ));
            return false;
        }

        if(request.startTime().isAfter(request.endTime())){
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "FlowSchedule",
                    "스케줄 시작시간이 종료 시간보다 늦을 수 없습니다."
            ));
            return false;
        }
        return true;
    }
}
