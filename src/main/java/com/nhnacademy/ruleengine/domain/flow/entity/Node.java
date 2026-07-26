package com.nhnacademy.ruleengine.domain.flow.entity;

import com.nhnacademy.ruleengine.domain.flow.dto.node.NodeInfo;
import com.nhnacademy.ruleengine.domain.flow.enums.NodeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "node_config", nullable = false)
    private String nodeConfig;

    @Column(name = "cooldown_sec")
    private Integer cooldownSec;

    public Node(Flow flow, String nodeName, NodeType nodeType, String nodeConfig, Integer cooldownSec) {
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