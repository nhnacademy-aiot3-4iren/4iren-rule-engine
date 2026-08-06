package com.nhnacademy.ruleengine.domain.flow.entity;

import com.nhnacademy.ruleengine.domain.flow.dto.ConnectionInfo;
import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Entity
@Table(
        name = "connections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_connections_source_target",
                        columnNames = { "source_node_id", "target_node_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connection_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false)
    private Flow flow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_node_id", nullable = false)
    private Node sourceNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_node_id", nullable = false)
    private Node targetNode;


    @Builder
    public Connection(Flow flow, Node sourceNode, Node targetNode, ConditionResult conditionResult) {
        this.flow = flow;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    public static Connection create(Flow flow, Node sourceNode, Node targetNode){
        return Connection.builder()
                .flow(flow).sourceNode(sourceNode).targetNode(targetNode).build();
    }
}