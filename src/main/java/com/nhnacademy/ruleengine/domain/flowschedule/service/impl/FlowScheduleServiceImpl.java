package com.nhnacademy.ruleengine.domain.flowschedule.service.impl;


import com.nhnacademy.ruleengine.common.exception.notfound.FlowNotFoundException;
import com.nhnacademy.ruleengine.common.exception.notfound.FlowScheduleNotFoundException;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.dto.*;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import com.nhnacademy.ruleengine.domain.flowschedule.repository.FlowScheduleRepository;
import com.nhnacademy.ruleengine.domain.flowschedule.service.FlowScheduleService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class FlowScheduleServiceImpl implements FlowScheduleService {
    private final FlowRepository flowRepository;
    private final FlowScheduleRepository flowScheduleRepository;

    @Override
    public FlowScheduleCreateResponse createFlowSchedule(Long roomId, Long flowId, FlowScheduleCreateRequest request) {
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(()->new FlowNotFoundException(flowId));

        FlowSchedule flowSchedule = FlowSchedule.create(flow, request);
        FlowSchedule savedFlowSchedule = flowScheduleRepository.save(flowSchedule);

        return FlowScheduleCreateResponse.of(savedFlowSchedule.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public FlowScheduleListResponse getFlowScheduleList(Long flowId) {
        List<FlowSchedule> flowScheduleList = flowScheduleRepository.findAllByFlowId(flowId);

        return FlowScheduleListResponse.from(flowId, flowScheduleList);
    }

    @Transactional(readOnly = true)
    @Override
    public FlowScheduleResponse getFlowScheduleDetail(Long scheduleId) {
        FlowSchedule flowSchedule = flowScheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new FlowScheduleNotFoundException(scheduleId));

        return FlowScheduleResponse.from(flowSchedule);
    }

    @Override
    public void deleteFlowSchedule( Long scheduleId) {
        flowScheduleRepository.deleteById(scheduleId);
    }

    private void saveSchedules(Flow savedFlow, @NotNull List<FlowScheduleCreateRequest> schedules) {
        if(schedules.isEmpty()){
            return;
        }

        List<FlowSchedule> scheduleList = schedules.stream()
                .map(s-> FlowSchedule.builder()
                        .flow(savedFlow)
                        .dayOfWeek(s.dayOfWeek())
                        .startTime(s.startTime())
                        .endTime(s.endTime()).build())
                .toList();

        List<FlowSchedule> savedScheduleList = flowScheduleRepository.saveAll(scheduleList);
    }

}
