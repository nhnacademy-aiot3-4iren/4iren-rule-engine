package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlowRepository extends JpaRepository<Flow, Long > {
    List<Flow> findByIsTemplate(Boolean isTemplate);
    List<Flow> findByRoomId(Long roomId);

    List<Flow> findAllByRoomIdAndIsTemplateFalse(Long roomId);

    Optional<Flow> findByFlowIdAndRoomIdAndIsTemplateFalse(Long flowId, Long roomId);

    List<Flow> findAllByIsTemplateTrue();

    Optional<Flow> findByFlowIdAndIsTemplateTrue(Long flowId);
}
