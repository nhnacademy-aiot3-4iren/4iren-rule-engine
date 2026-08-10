package com.nhnacademy.ruleengine.domain.flowschedule.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flowschedule.entity.FlowSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlowScheduleRepository extends JpaRepository<FlowSchedule,Long> {

    List<FlowSchedule> findAllByFlowId(Long flowId);

    @Query("""
         SELECT COUNT(fs) > 0 FROM FlowSchedule fs
         WHERE fs.id = :id 
         AND fs.flow.id = :flowId
         AND fs.flow.roomId = :roomId
    """)
    boolean existsFlowSchedule(@Param("id") Long id,
                               @Param("flowId") Long flowId,
                               @Param("roomId") Long roomId);

    @Query("""
         SELECT fs FROM FlowSchedule fs
         WHERE fs.id = :id 
         AND fs.flow.id = :flowId
         AND fs.flow.roomId = :roomId
    """)
    Optional<FlowSchedule> findSchedule(@Param("id") Long id,
                                        @Param("flowId") Long flowId,
                                        @Param("roomId") Long roomId);

    List<Flow> findAllByFlowIdIn(List<Long> flowIds);
}