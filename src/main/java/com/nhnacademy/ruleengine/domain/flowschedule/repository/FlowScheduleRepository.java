package com.nhnacademy.ruleengine.domain.flowschedule.repository;

import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlowScheduleRepository extends JpaRepository<FlowSchedule,Long> {

    List<FlowSchedule> findAllByFlowId(Long flowId);
    void deleteAllByFlowId(Long flowId);
}
