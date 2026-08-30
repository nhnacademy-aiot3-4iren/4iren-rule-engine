package com.nhnacademy.ruleengine.domain.flow.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.flow.entity.Flow;
import com.nhnacademy.ruleengine.domain.flow.entity.Node;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.NodeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import(JacksonAutoConfiguration.class)
class NodeRepositoryTest {
    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Flow flow1;
    private Flow flow2;

    @BeforeEach
    void setUp() {
        flow1 = flowRepository.save(Flow.regularBuilder()
                .roomId(1L).isActive(true).flowName("플로우1").description("플로우1 설명").build());

        flow2 = flowRepository.save(Flow.regularBuilder()
                .roomId(1L).isActive(true).flowName("플로우2").description("플로우2 설명").build());


        String thresholdJson = """
                 {
                                "nodeType": "THRESHOLD",
                                "x": 100,
                                "y": 150,
                                "measurementType": "CO2",
                                "unit": "ppm",
                                "operator": "GT",
                                "threshold": 1000.0
                              }
                """;

        String avgJson = """
                {
                        "nodeType": "AVERAGE",
                        "x": 120,
                        "y": 200,
                        "measurementType": "TEMPERATURE",
                        "unit": "°C",
                        "operator": "GTE",
                        "average": 28.5,
                        "windowSec": 600
                      }
                """;
        String alertJson = """
                {
                        "nodeType": "ALERT",
                        "x": 300,
                        "y": 150,
                        "channel": "TELEGRAM",
                        "alertTitle": "실내 이산화탄소 수치 초과!",
                        "alertType": "COMFORT_LIMIT_EXCEEDED"
                      }
                """;



        try {
        nodeRepository.save(Node.builder().flow(flow1).nodeName("노드1").nodeType(NodeType.THRESHOLD).nodeConfig(objectMapper.readValue(thresholdJson, NodeConfig.class)).cooldownSec(500).build());
        nodeRepository.save(Node.builder().flow(flow1).nodeName("노드2").nodeType(NodeType.ALERT).nodeConfig(objectMapper.readValue(alertJson, NodeConfig.class)).cooldownSec(300).build());
        nodeRepository.save(Node.builder().flow(flow2).nodeName("노드3").nodeType(NodeType.AVERAGE).nodeConfig(objectMapper.readValue(alertJson, NodeConfig.class)).cooldownSec(300).build());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    @DisplayName("findAllByFlowId - 해당 플로우 노드만 반환")
    void findAllByFlowId_returnsNodesOfFlow() {
        List<Node> result = nodeRepository.findAllByFlowId(flow1.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Node::getNodeName)
                .containsExactlyInAnyOrder("노드1", "노드2");

        assertThat(result)
                .extracting(Node::getNodeName)
                .doesNotContain("노드3");
    }

    @Test
    @DisplayName("deleteAllByFlowId - 해당 플로우 노드 삭제")
    void deleteAllByFlowId_deletesNodesOfFlow() {
        nodeRepository.deleteAllByFlowId(flow1.getId());

        List<Node> result1 = nodeRepository.findAllByFlowId(flow1.getId());
        assertThat(result1).isEmpty();

        List<Node> result2 = nodeRepository.findAllByFlowId(flow2.getId());
        assertThat(result2).hasSize(1);
    }

    @Test
    @DisplayName("findAllByFlowIdIn - 여러 플로우 노드 한번에 조회")
    void findAllByFlowIdIn_returnsAllNodes() {
        List<Node> result = nodeRepository.findAllByFlowIdIn(
                List.of(flow1.getId(), flow2.getId())
        );

        assertThat(result).hasSize(3);
    }

}