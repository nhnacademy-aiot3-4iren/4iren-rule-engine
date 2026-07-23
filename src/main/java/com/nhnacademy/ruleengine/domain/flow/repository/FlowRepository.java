package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlowRepository extends JpaRepository<Flow, Long > {

    List<Flow> findAllByRoomId(Long roomId);

    List<Flow> findAllByIsTemplate(boolean isTemplate);
}
