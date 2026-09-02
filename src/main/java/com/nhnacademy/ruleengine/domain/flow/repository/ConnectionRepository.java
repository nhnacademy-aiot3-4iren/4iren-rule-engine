package com.nhnacademy.ruleengine.domain.flow.repository;

import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    List<Connection> findAllBySourceNodeFlowId(Long flowId);


    @Modifying
    @Query(value = """
            DELETE FROM connections
            WHERE source_node_id IN (SELECT node_id FROM nodes WHERE flow_id =:flowId)
                OR target_node_id IN (SELECT node_id FROM nodes WHERE flow_id =:flowId)               
            """, nativeQuery = true)
    void deleteAllByNodeFlowId(Long flowId);

    @Query("""
        SELECT DISTINCT c
        FROM Connection c
        JOIN FETCH c.sourceNode sn
        JOIN FETCH sn.flow f
        JOIN FETCH c.targetNode tn
        WHERE f.id IN :flowIds
    """)
    List<Connection> findAllBySourceNodeFlowIdIn(List<Long> flowIds);
}
