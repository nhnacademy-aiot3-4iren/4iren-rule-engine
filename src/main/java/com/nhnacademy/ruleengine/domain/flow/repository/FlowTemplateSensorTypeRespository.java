package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.FlowTemplateSensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlowTemplateSensorTypeRespository extends JpaRepository<FlowTemplateSensorType, Long> {

}
