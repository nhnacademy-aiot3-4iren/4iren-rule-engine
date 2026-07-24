package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.FlowTemplateSensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlowTemplateSensorTypeRespository extends JpaRepository<FlowTemplateSensorType, Long> {
    List<FlowTemplateSensorType> findAllByFlow(Flow flow);

    //템플릿 플로우 리스트에 포함된 모든 센서 정보들을 가져옴
    List<FlowTemplateSensorType> findAllByFlowIn(List<Flow> flowTemplates);

}
