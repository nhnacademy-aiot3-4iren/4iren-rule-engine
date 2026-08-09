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
        Flow flow = flowRepository.findByIdAndRoomId(flowId, roomId).orElseThrow(FlowNotFoundException::new);

        FlowSchedule flowSchedule = FlowSchedule.create(flow, request);
        FlowSchedule savedFlowSchedule = flowScheduleRepository.save(flowSchedule);

        return FlowScheduleCreateResponse.of(savedFlowSchedule.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public FlowScheduleListResponse getFlowScheduleList(Long flowId) {
        if (!flowRepository.existsById(flowId)) {
            throw new FlowNotFoundException();
        }
        List<FlowSchedule> flowScheduleList = flowScheduleRepository.findAllByFlowId(flowId);

        return FlowScheduleListResponse.from(flowId, flowScheduleList);
    }

    @Transactional(readOnly = true)
    @Override
    public FlowScheduleResponse getFlowScheduleDetail(Long scheduleId) {
        FlowSchedule flowSchedule = flowScheduleRepository.findById(scheduleId)
                .orElseThrow(FlowScheduleNotFoundException::new);

        return FlowScheduleResponse.from(flowSchedule);
    }

    @Override
    public void deleteFlowSchedule( Long scheduleId) {
        if (!flowScheduleRepository.existsById(scheduleId)) {
            throw new FlowScheduleNotFoundException();
        }
        flowScheduleRepository.deleteById(scheduleId);
    }


}
