package com.nhnacademy.ruleengine.domain.flow.dto.response;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import com.nhnacademy.ruleengine.domain.flow.enums.SensorType;
import com.nhnacademy.ruleengine.domain.flow.repository.FlowReposiroty;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


public record FlowResponse(
        Long flowId,
        String flowName,
        String description,
        boolean hasSchedule,
        boolean isActive
) {

    static public FlowResponse from(Flow flow, boolean hasSchedule){
        return new FlowResponse(flow.getId(), flow.getFlowName(), flow.getDescription(), hasSchedule, flow.getIsActive());
    }

}