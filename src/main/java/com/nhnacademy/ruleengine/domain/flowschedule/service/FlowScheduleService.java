package com.nhnacademy.ruleengine.domain.flowschedule.service;


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
    @CacheEvict(value = "flow:room", key = "#roomId", cacheManager = "flowCacheManager")
    public FlowScheduleResponse getFlowScheduleDetail(Long roomId, Long flowId, Long scheduleId) {

        FlowSchedule flowSchedule = flowScheduleRepository.findSchedule(scheduleId, flowId, roomId)
                .orElseThrow(FlowScheduleNotFoundException::new);

        return FlowScheduleResponse.from(flowSchedule);
    }

    public void deleteFlowSchedule( Long roomId, Long flowId, Long scheduleId) {
        if (!flowScheduleRepository.existsFlowSchedule(scheduleId, flowId, roomId)) {
            throw new FlowScheduleNotFoundException();
        }
        flowScheduleRepository.deleteById(scheduleId);
    }
}
