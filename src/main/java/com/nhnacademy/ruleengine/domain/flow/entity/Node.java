package com.nhnacademy.ruleengine.domain.flow.entity;

import com.nhnacademy.ruleengine.domain.flow.dto.NodeInfo;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.converter.NodeConfigConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
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

//    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "node_config", nullable = false)
    @Convert(converter = NodeConfigConverter.class)
    private NodeConfig nodeConfig;

    @Column(name = "cooldown_sec")
    private Integer cooldownSec;

    @OneToMany(mappedBy = "sourceNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> outgoingConnections = new ArrayList<>();

    @OneToMany(mappedBy = "targetNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Connection> incomingConnections = new ArrayList<>();

    public Node(Flow flow, String nodeName, NodeType nodeType, NodeConfig nodeConfig, Integer cooldownSec) {
        this.flow = flow;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.nodeConfig = nodeConfig;
        this.cooldownSec = cooldownSec;
    }
    public static Node create(Flow flow, NodeInfo nodeInfo){
        return Node.builder().flow(flow)
                .nodeName(nodeInfo.nodeName())
                .nodeType(nodeInfo.nodeType())
                .nodeConfig(nodeInfo.nodeConfig())
                .cooldownSec(nodeInfo.cooldownSec())
                .build();
    }

    public void update(NodeInfo nodeInfo) {
        if (nodeName != null) this.nodeName = nodeInfo.nodeName();
        if (nodeType != null) this.nodeType = nodeInfo.nodeType();
        if (nodeConfig != null) this.nodeConfig = nodeInfo.nodeConfig();
        if (cooldownSec != null) this.cooldownSec = nodeInfo.cooldownSec();
    }
}