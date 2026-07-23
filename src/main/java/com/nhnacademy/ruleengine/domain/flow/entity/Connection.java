package com.nhnacademy.ruleengine.domain.flow.entity;

import com.nhnacademy.ruleengine.domain.flow.enums.ConditionResult;
import jakarta.persistence.*;
import lombok.*;

@Builder
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

    @Enumerated(EnumType.STRING)
    @Column(name = "conditions_result", nullable = false, length = 10)
    private ConditionResult conditionResult;


    public Connection(Flow flow, Node sourceNode, Node targetNode, ConditionResult conditionResult) {
        this.flow = flow;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.conditionResult =conditionResult;
    }
}