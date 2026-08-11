package com.nhnacademy.ruleengine.domain.flow.entity;

import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.converter.NodeConfigConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "nodes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false)
    private Flow flow;

    @Column(name = "node_name", nullable = false, length = 50)
    private String nodeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 20)
    private NodeType nodeType;

    @Column(name = "node_config", nullable = false)
    @Convert(converter = NodeConfigConverter.class)
    private NodeConfig nodeConfig;

    @Column(name = "cooldown_sec")
    private Integer cooldownSec;

    @OneToMany(mappedBy = "targetNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> incomingConnections = new ArrayList<>();

    @OneToMany(mappedBy = "sourceNode", cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction("branch_type = 'TRUE'")
    private List<Connection> trueOutgoingConnections = new ArrayList<>();

    @OneToMany(mappedBy = "sourceNode", cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction("branch_type = 'FALSE'")
    private List<Connection> falseOutgoingConnections = new ArrayList<>();

    @Builder
    public Node(Flow flow, String nodeName, NodeType nodeType, NodeConfig nodeConfig, Integer cooldownSec) {
        this.flow = flow;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.nodeConfig = nodeConfig;
        this.cooldownSec = cooldownSec;
    }
}