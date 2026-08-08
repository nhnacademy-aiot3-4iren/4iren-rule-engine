package com.nhnacademy.ruleengine.domain.templateflow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.templateflow.entity.FlowTemplateMeasurementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlowTemplateMeasurementTypeRespository extends JpaRepository<FlowTemplateMeasurementType, Long> {
    List<FlowTemplateMeasurementType> findAllByFlow(Flow flow);

    //템플릿 플로우 리스트에 포함된 모든 센서 정보들을 가져옴
    List<FlowTemplateMeasurementType> findAllByFlowIn(List<Flow> flowTemplates);

    void deleteAllByFlow(Flow templateFlow);
}
