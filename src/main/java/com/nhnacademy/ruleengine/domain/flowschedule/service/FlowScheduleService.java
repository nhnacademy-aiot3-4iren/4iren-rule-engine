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
import java.util.*;
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
    private void validateCreateRequest(Long flowId, List<FlowScheduleCreateRequest.FlowScheduleRequest> requests){
        List<ValidationErrorResponse.ValidationError> errors = new ArrayList<>();

        requests.forEach(request -> validateTimeRange(request, errors));

        if(errors.isEmpty()){
            validateNoOverlapInRequest(requests, errors);
            validateNoOverlapWithExisting(flowId, requests, errors);
        }
        if (!errors.isEmpty()) {
            throw new FlowScheduleValidationFailed(errors);
        }
    }

    //시작시간 종료시간 범위 검증
    private void validateTimeRange(FlowScheduleCreateRequest.FlowScheduleRequest request,
                                   List<ValidationErrorResponse.ValidationError> errors) {
        if (request.startTime().equals(request.endTime())) {
            errors.add(ValidationErrorResponse.ValidationError.of(
                    request.dayOfWeek().name(),
                    "시작 시간과 종료 시간을 다르게 설정해야 합니다."
            ));
            return;
        }

        if (request.startTime().isAfter(request.endTime())) {
            errors.add(ValidationErrorResponse.ValidationError.of(
                    request.dayOfWeek().name(),
                    "시작 시간은 종료 시간보다 빠르게 설정해야 합니다."
            ));
        }
    }


    /**
     * [요청 내 중복 검사]
     * 동일한 요청(Request) 리스트 안에 포함된 스케줄 상호 간에 시간이 겹치는지 검증합니다.
     *
     * 1. 요일(DayOfWeek)별로 그룹화한 뒤, 시작 시간(startTime) 기준으로 오름차순 정렬합니다.
     * 2. 연속된 스케줄을 비교하여 '현재 스케줄의 시작 시간 < 이전 스케줄의 종료 시간'인 경우 중복으로 처리합니다.
     *
     * @param requests 검증할 스케줄 요청 목록
     * @param errors   검증 실패 시 에러 정보를 담을 리스트
     */
    private void validateNoOverlapInRequest(
            List<FlowScheduleCreateRequest.FlowScheduleRequest> requests,
            List<ValidationErrorResponse.ValidationError> errors
    ) {
        //요일(DayOfWeek)별로 그룹화
        Map<DayOfWeek, List<FlowScheduleCreateRequest.FlowScheduleRequest>> schedulesByDay = requests.stream()
                .collect(Collectors.groupingBy(FlowScheduleCreateRequest.FlowScheduleRequest::dayOfWeek));


        schedulesByDay.forEach((day, dayRequests) -> {
            //시작 시간(startTime) 기준으로 오름차순 정렬
            List<FlowScheduleCreateRequest.FlowScheduleRequest> sorted = dayRequests.stream()
                    .sorted(Comparator.comparing(FlowScheduleCreateRequest.FlowScheduleRequest::startTime))
                    .toList();

            for (int i = 1; i < sorted.size(); i++) {
                //연속된 스케줄을 비교하여 '현재 스케줄의 시작 시간 < 이전 스케줄의 종료 시간'인 경우 중복으로 처리
                FlowScheduleCreateRequest.FlowScheduleRequest prev = sorted.get(i - 1);
                FlowScheduleCreateRequest.FlowScheduleRequest curr = sorted.get(i);

                if (curr.startTime().isBefore(prev.endTime())) {
                    errors.add(ValidationErrorResponse.ValidationError.of(
                            day.name(),
                            "요청 목록 안에 겹치는 실행 시간이 있습니다."
                    ));
                    return;
                }
            }
        });
    }

    /**
     * [기존 DB 스케줄과 중복 검사]
     * 요청된 스케줄이 이미 DB에 저장되어 있는 기존 스케줄과 시간이 겹치는지 검증합니다.
     *
     * 각 요청 스케줄의 요일별 기존 데이터를 조회하여, 시간 구간(Interval) 겹침 공식인
     * '(요청 시작 < 기존 종료) AND (요청 종료 > 기존 시작)' 조건 충족 여부를 확인합니다.
     *
     * @param flowId   대상 플로우 ID
     * @param requests 검증할 스케줄 요청 목록
     * @param errors   검증 실패 시 에러 정보를 담을 리스트
     */
    private void validateNoOverlapWithExisting(
            Long flowId,
            List<FlowScheduleCreateRequest.FlowScheduleRequest> requests,
            List<ValidationErrorResponse.ValidationError> errors
    ) {
        // 1. 요청받은 요일 목록 추출
        Set<DayOfWeek> targetDays = requests.stream()
                .map(FlowScheduleCreateRequest.FlowScheduleRequest::dayOfWeek)
                .collect(Collectors.toSet());

        // 2. DB 쿼리 1회로 해당 요일들의 기존 스케줄을 한 번에 가져와 요일별로 그룹화
        Map<DayOfWeek, List<FlowSchedule>> existingSchedulesByDay = flowScheduleRepository
                .findAllByFlowIdAndDayOfWeekIn(flowId, targetDays).stream()
                .collect(Collectors.groupingBy(FlowSchedule::getDayOfWeek));

        requests.forEach(request -> {
            //flowId, 요일 기준 기존 스케줄 목록 조회
            List<FlowSchedule> existingSchedules = existingSchedulesByDay.getOrDefault(request.dayOfWeek(), List.of());

            boolean isOverlapped = existingSchedules.stream().anyMatch(existing ->
                    request.startTime().isBefore(existing.getEndTime()) &&
                    request.endTime().isAfter(existing.getStartTime())
            );

            if(isOverlapped){
                errors.add(ValidationErrorResponse.ValidationError.of(
                        request.dayOfWeek().name(),
                        "해당 요일에 겹치는 실행 시간이 이미 존재합니다."
                ));
            }

        });
    }


}
