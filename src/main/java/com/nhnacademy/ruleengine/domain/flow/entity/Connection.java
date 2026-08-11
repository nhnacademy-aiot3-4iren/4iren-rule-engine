package com.nhnacademy.ruleengine.domain.flow.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "connections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_connections_source_target_branch",
                        columnNames = { "source_node_id", "target_node_id", "branch_type" }
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

    @Column(name = "branch_type", nullable = false, length = 20)
    private String branchType;

    @Builder
    public Connection(Flow flow, Node sourceNode, Node targetNode, String branchType) {
        this.flow = flow;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.branchType = (branchType != null && !branchType.isBlank()) ? branchType.toUpperCase() : "TRUE";
    }

    public static Connection create(Flow flow, Node sourceNode, Node targetNode, String branchType) {
        return Connection.builder()
                .flow(flow)
                .sourceNode(sourceNode)
                .targetNode(targetNode)
                .branchType(branchType)
                .build();
    }
}