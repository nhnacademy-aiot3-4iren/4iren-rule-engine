package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlowRepository extends JpaRepository<Flow, Long > {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select f
    from Flow f
    where f.id = :flowId
      and f.roomId = :roomId
    """)
    Optional<Flow> findByIdAndRoomIdForUpdate(@Param("flowId") Long flowId,@Param("roomId") Long roomId);

    Optional<Flow> findByIdAndRoomId(Long flowId, Long roomId);

    boolean existsByIdAndRoomId(Long flowId, Long roomId);

    List<Flow> findAllByRoomId(Long roomId);

    List<Flow> findAllByIsTemplate(boolean isTemplate);

    List<Flow> findAllByRoomIdAndIsActiveTrueAndIsTemplateFalse(Long roomId);

    long countByRoomIdAndIsActiveTrueAndIsTemplateFalse(Long roomId);
}
