package com.nhnacademy.ruleengine.domain.flowschedule.service;


import com.nhnacademy.ruleengine.common.advice.ValidationErrorResponse;
import com.nhnacademy.ruleengine.common.exception.invalid.FlowScheduleValidationFailed;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowScheduleNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateRequest;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleCreateResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleListResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.FlowScheduleResponse;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowScheduleService {
    private final FlowRepository flowRepository;
    private final FlowScheduleRepository flowScheduleRepository;

    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public FlowScheduleCreateResponse createFlowSchedule(Long roomId, Long flowId, FlowScheduleCreateRequest request) {
        validateRequestList(request);

        log.info("플로우 스케줄 생성 처리 시작 roomId={}, flowId={}, scheduleCount={}",
                roomId, flowId, request.flowScheduleRequestList().size());
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);

        validateCreateRequest(flowId, request.flowScheduleRequestList());

        List<FlowSchedule> flowScheduleList = request.flowScheduleRequestList().stream()
                .map(flowScheduleRequest -> FlowSchedule.create(flow,flowScheduleRequest))
                .toList();

        List<FlowSchedule> savedFlowScheduleList = flowScheduleRepository.saveAll(flowScheduleList);
        List<Long> scheduleIds = savedFlowScheduleList.stream()
                .map(FlowSchedule::getId)
                .toList();

        log.info("플로우 스케줄 생성 완료 roomId={}, flowId={}, scheduleCount={}", roomId, flowId, savedFlowScheduleList.size());
        return FlowScheduleCreateResponse.of(scheduleIds);
    }

    @Transactional(readOnly = true)
    public FlowScheduleListResponse getFlowScheduleList(Long flowId, Long roomId) {
        if (!flowRepository.existsByIdAndRoomId(flowId, roomId)) {
            throw new FlowNotFoundException();
        }
        List<FlowSchedule> flowScheduleList = flowScheduleRepository.findAllByFlowId(flowId);

        log.info("플로우 스케줄 목록 조회 완료 roomId={}, flowId={}, scheduleCount={}",
                roomId, flowId, flowScheduleList.size());
        return FlowScheduleListResponse.from(flowId, flowScheduleList);
    }

    @Transactional(readOnly = true)
    public FlowScheduleResponse getFlowScheduleDetail(Long roomId, Long flowId, Long scheduleId) {

        FlowSchedule flowSchedule = flowScheduleRepository.findSchedule(scheduleId, flowId, roomId)
                .orElseThrow(FlowScheduleNotFoundException::new);

        log.info("플로우 스케줄 상세 조회 완료 roomId={}, flowId={}, scheduleId={}", roomId, flowId, scheduleId);
        return FlowScheduleResponse.from(flowSchedule);
    }

    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public void deleteFlowSchedule( Long roomId, Long flowId, Long scheduleId) {
        if (!flowScheduleRepository.existsFlowSchedule(scheduleId, flowId, roomId)) {
            throw new FlowScheduleNotFoundException();
        }
        flowScheduleRepository.deleteById(scheduleId);
        log.info("플로우 스케줄 삭제 완료 roomId={}, flowId={}, scheduleId={}", roomId, flowId, scheduleId);
    }

    //검증 코드
    private void validateRequestList(FlowScheduleCreateRequest request) {
        if (request == null || request.flowScheduleRequestList() == null || request.flowScheduleRequestList().isEmpty()) {
            throw new FlowScheduleValidationFailed(List.of(
                    ValidationErrorResponse.ValidationError.of(
                            "flowScheduleRequestList",
                            "스케줄 목록은 비어 있을 수 없습니다."
                    )
            ));
        }
    }

    private void validateCreateRequest(Long flowId, List<FlowScheduleCreateRequest.FlowScheduleRequest> requests){
        List<ValidationErrorResponse.ValidationError> errors = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            FlowScheduleCreateRequest.FlowScheduleRequest request = requests.get(i);
            validateRequired(i, request, errors);
            validationTimeRange(i, request, errors);
        }

        if(errors.isEmpty()){
            validateNoOverlapInRequest(requests, errors);
            validateNoOverlap(flowId, requests, errors);
        }
        if (!errors.isEmpty()) {
            throw new FlowScheduleValidationFailed(errors);
        }
    }

    private void validateRequired(int index,
                                  FlowScheduleCreateRequest.FlowScheduleRequest request,
                                  List<ValidationErrorResponse.ValidationError> errors) {
        if (request == null) {
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "flowScheduleRequestList[" + index + "]",
                    "스케줄 정보는 비어 있을 수 없습니다."
            ));
            return;
        }

        if (request.dayOfWeek() == null) {
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "flowScheduleRequestList[" + index + "].dayOfWeek",
                    "요일은 필수입니다."
            ));
        }

        if (request.startTime() == null) {
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "flowScheduleRequestList[" + index + "].startTime",
                    "시작 시간은 필수입니다."
            ));
        }

        if (request.endTime() == null) {
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "flowScheduleRequestList[" + index + "].endTime",
                    "종료 시간은 필수입니다."
            ));
        }
    }

    //요청 스케줄 리스트 각각의 시간대 중복 검사
    private void validateNoOverlapInRequest(List<FlowScheduleCreateRequest.FlowScheduleRequest> requests,
                                            List<ValidationErrorResponse.ValidationError> errors) {
        Map<DayOfWeek, List<FlowScheduleCreateRequest.FlowScheduleRequest>> schedulesByDay = requests.stream()
                .collect(Collectors.groupingBy(FlowScheduleCreateRequest.FlowScheduleRequest::dayOfWeek));

        for (Map.Entry<DayOfWeek, List<FlowScheduleCreateRequest.FlowScheduleRequest>> entry : schedulesByDay.entrySet()) {
            List<FlowScheduleCreateRequest.FlowScheduleRequest> sortedSchedules = entry.getValue().stream()
                    .sorted(Comparator.comparing(FlowScheduleCreateRequest.FlowScheduleRequest::startTime))
                    .toList();

            for (int i = 1; i < sortedSchedules.size(); i++) {
                FlowScheduleCreateRequest.FlowScheduleRequest previous = sortedSchedules.get(i - 1);
                FlowScheduleCreateRequest.FlowScheduleRequest current = sortedSchedules.get(i);

                if (current.startTime().isBefore(previous.endTime())) {
                    errors.add(ValidationErrorResponse.ValidationError.of(
                            "FlowSchedule",
                            "%s 요청 목록 안에 겹치는 실행 시간이 있습니다.".formatted(entry.getKey())
                    ));
                    return;
                }
            }
        }
    }

    //기존 스케줄 시간대 겹침 여부 검증
    private void validateNoOverlap(Long flowId,
                                   List<FlowScheduleCreateRequest.FlowScheduleRequest> requests,
                                   List<ValidationErrorResponse.ValidationError> errors) {
        for (FlowScheduleCreateRequest.FlowScheduleRequest request : requests) {
            List<FlowSchedule> schedules = flowScheduleRepository.findAllByFlowIdAndDayOfWeek(flowId, request.dayOfWeek());

            boolean overlapped = schedules.stream()
                    .anyMatch(schedule ->
                                request.startTime().isBefore(schedule.getEndTime())
                            && request.endTime().isAfter(schedule.getStartTime())
                    );

            if(overlapped){
                errors.add(ValidationErrorResponse.ValidationError.of(
                        "FlowSchedule",
                        "%s 같은 요일에 겹치는 실행 시간이 이미 있습니다.".formatted(request.dayOfWeek())
                ));
                return;
            }
        }
    }

    //시작시간 종료시간 범위 검증
    private void validationTimeRange(int index,
                                     FlowScheduleCreateRequest.FlowScheduleRequest request,
                                     List<ValidationErrorResponse.ValidationError> errors) {
        if(request == null || request.startTime() == null || request.endTime() == null){
            return;
        }


        if(request.startTime().equals(request.endTime())){
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "flowScheduleRequestList[" + index + "]",
                    "시작 시간과 종료 시간을 다르게 설정해야 합니다."
            ));
            return;
        }

        if(request.startTime().isAfter(request.endTime())){
            errors.add(ValidationErrorResponse.ValidationError.of(
                    "flowScheduleRequestList[" + index + "]",
                    "시작 시간은 종료 시간보다 빠르게 설정해야 합니다."
            ));
        }
    }
}
