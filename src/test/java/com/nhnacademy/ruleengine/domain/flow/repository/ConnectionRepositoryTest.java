package com.nhnacademy.ruleengine.domain.flow.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ruleengine.domain.flow.entity.Connection;
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
class ConnectionRepositoryTest {

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private FlowRepository flowRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Flow flow1;
    private Flow flow2;
    private Connection conn1;
    private Connection conn2;
    private Connection conn3;


    @BeforeEach
    void setUp() {
        flow1 = flowRepository.save(Flow.regularBuilder().roomId(1L).isActive(true).flowName("플로우1").description("플로우1 설명").build());
        flow2 = flowRepository.save(Flow.regularBuilder().roomId(1L).isActive(true).flowName("플로우2").description("플로우2 설명").build());


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

        Node node1;
        Node node2;
        Node node3;
        Node node4;

        try {
            node1 = nodeRepository.save(Node.builder().flow(flow1).nodeName("노드1").nodeType(NodeType.THRESHOLD).nodeConfig(objectMapper.readValue(thresholdJson, NodeConfig.class)).build());
            node2 = nodeRepository.save(Node.builder().flow(flow1).nodeName("노드2").nodeType(NodeType.ALERT).nodeConfig(objectMapper.readValue(alertJson, NodeConfig.class)).build());
            node3 = nodeRepository.save(Node.builder().flow(flow2).nodeName("노드3").nodeType(NodeType.AVERAGE).nodeConfig(objectMapper.readValue(alertJson, NodeConfig.class)).build());
            node4 = nodeRepository.save(Node.builder().flow(flow2).nodeName("노드4").nodeType(NodeType.ALERT).nodeConfig(objectMapper.readValue(alertJson, NodeConfig.class)).build());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        conn1 = connectionRepository.save(Connection.builder()
                .sourceNode(node1).targetNode(node2).branchType("TRUE").build());
        conn2 = connectionRepository.save(Connection.builder()
                .sourceNode(node1).targetNode(node2).branchType("FALSE").build());
        conn3 = connectionRepository.save(Connection.builder()
                .sourceNode(node3).targetNode(node4).branchType("TRUE").build());

    }

    @Test
    @DisplayName("findAllBySourceNodeFlowId - 해당 플로우 커넥션만 반환")
    void findAllByFlowId_returnsConnectionsOfSourceNodeFlow() {
        List<Connection> result = connectionRepository.findAllBySourceNodeFlowId(flow1.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Connection::getId)
                .doesNotContain(conn3.getId());
    }
    @Test
    @DisplayName("deleteAllByNodeFlowId - 해당 플로우 커넥션 삭제")
    void deleteAllByFlowId_deletesConnectionsOfNodeFlow() {
        connectionRepository.deleteAllByNodeFlowId(flow1.getId());

        List<Connection> result1 = connectionRepository.findAllBySourceNodeFlowId(flow1.getId());
        assertThat(result1).isEmpty();

        List<Connection> result2 = connectionRepository.findAllBySourceNodeFlowId(flow2.getId());
        assertThat(result2).hasSize(1);
    }


    @Test
    @DisplayName("findAllBySourceNodeFlowIdIn - 여러 플로우 커넥션 한번에 조회")
    void findAllByFlowIdIn_returnsAllConnectionsSourceNodeSourceNode() {
        List<Connection> result = connectionRepository.findAllBySourceNodeFlowIdIn(
                List.of(flow1.getId(), flow2.getId())
        );

        assertThat(result).hasSize(3);
    }
    @Test
    @DisplayName("findAllBySourceNodeFlowIdIn - 빈 리스트 입력 시 빈 결과")
    void findAllBySourceNodeFlowIdIn_emptyInput_returnsEmpty() {
        List<Connection> result = connectionRepository.findAllBySourceNodeFlowIdIn(List.of());

        assertThat(result).isEmpty();
    }

}