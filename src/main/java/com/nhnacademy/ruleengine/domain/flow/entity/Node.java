package com.nhnacademy.ruleengine.domain.flow.entity;

import com.nhnacademy.ruleengine.domain.flow.dto.NodeInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.templateflow.dto.TemplateNodeInfo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "node_config",columnDefinition = "json", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private NodeConfig nodeConfig;


    @OneToMany(mappedBy = "targetNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> incomingConnections = new ArrayList<>();

    @OneToMany(mappedBy = "sourceNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> outgoingConnections = new ArrayList<>();

    @Builder
    public Node(Flow flow, String nodeName, NodeType nodeType, NodeConfig nodeConfig ) {
        this.flow = flow;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.nodeConfig = nodeConfig;
    }

    public static Node create(Flow flow, NodeInfo nodeInfo){
        return Node.builder().flow(flow)
                .nodeName(nodeInfo.nodeName())
                .nodeType(nodeInfo.nodeType())
                .nodeConfig(nodeInfo.nodeConfig())
                .build();
    }
    public static Node create(Flow flow, TemplateNodeInfo nodeInfo){
        return Node.builder().flow(flow)
                .nodeName(nodeInfo.nodeName())
                .nodeType(nodeInfo.nodeType())
                .nodeConfig(nodeInfo.nodeConfig())
                .build();
    }

    @Transient
    public List<Connection> getTrueOutgoingConnections() {
        return outgoingConnections.stream()
                .filter(c -> "TRUE".equalsIgnoreCase(c.getBranchType()))
                .toList();
    }

    @Transient
    public List<Connection> getFalseOutgoingConnections() {
        return outgoingConnections.stream()
                .filter(c -> "FALSE".equalsIgnoreCase(c.getBranchType()))
                .toList();
    }
}