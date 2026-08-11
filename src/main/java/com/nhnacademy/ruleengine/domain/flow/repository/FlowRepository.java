package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface FlowRepository extends JpaRepository<Flow, Long > {

    Optional<Flow> findByIdAndRoomId(Long flowId, Long roomId);

    boolean existsByIdAndRoomId(Long flowId, Long roomId);

    List<Flow> findAllByRoomId(Long roomId);

    List<Flow> findAllByIsTemplate(boolean isTemplate);

    List<Flow> findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse(Long roomId);
}
