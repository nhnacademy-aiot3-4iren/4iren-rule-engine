package com.nhnacademy.ruleengine.engine.listener;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.ruleengine.domain.flow.enums.BranchType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertChannel;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.AlertType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.MeasurementType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.NodeType;
import com.nhnacademy.ruleengine.domain.nodeconfig.enums.Operator;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.action.AlertNodeConfig;
import com.nhnacademy.ruleengine.domain.nodeconfig.jsoninfo.condition.ThresholdNodeConfig;
import com.nhnacademy.ruleengine.engine.converter.SensorPayloadConverter;
import com.nhnacademy.ruleengine.engine.dispatcher.FlowDispatcher;
import com.nhnacademy.ruleengine.engine.executor.FlowExecutor;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutor;
import com.nhnacademy.ruleengine.engine.executor.node.NodeExecutorRegistry;
import com.nhnacademy.ruleengine.engine.executor.node.impl.AlertNodeExecutor;
import com.nhnacademy.ruleengine.engine.executor.node.impl.ThresholdNodeExecutor;
import com.nhnacademy.ruleengine.engine.filter.FlowScheduleFilter;
import com.nhnacademy.ruleengine.engine.flow.ExecutableFlow;
import com.nhnacademy.ruleengine.engine.flow.FlowLoader;
import com.nhnacademy.ruleengine.engine.handler.RuleEngineHandler;
import com.nhnacademy.ruleengine.engine.model.AlertEvent;
import com.nhnacademy.ruleengine.engine.publisher.AlertEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = SensorDataListenerIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
        "rabbitmq.exchange.name=test.alert.exchange",
        "ruleengine.routing-key.alert=test.alert.routing-key"
})
class SensorDataListenerIntegrationTest {

    private static final long ROOM_ID = 101L;
    private static final long FLOW_ID = 1L;
    private static final long START_NODE_ID = 10L;
    private static final long THRESHOLD_NODE_ID = 11L;
    private static final long ALERT_NODE_ID = 12L;
    private static final int DEDUP_WINDOW_SEC = 30;

    @Autowired
    private SensorDataListener listener;

    @MockitoBean
    private FlowLoader flowLoader;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("RabbitMQ raw sensor data 수신부터 threshold 통과 후 알림 발행까지 실행")
    void receiveSensorData_publishAlertWhenFlowConditionPasses() {
        ExecutableFlow flow = createThresholdToAlertFlow();
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of(flow));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("SENT"), eq(Duration.ofSeconds(DEDUP_WINDOW_SEC))))
                .thenReturn(true);

        listener.receiveSensorData(sensorPayload(31.5));

        ArgumentCaptor<AlertEvent> eventCaptor = ArgumentCaptor.forClass(AlertEvent.class);//AlertEvent타입의 데이터를 캡처하는 객체 선언
        verify(rabbitTemplate).convertAndSend(eq("test.alert.exchange"), eq("test.alert.routing-key"), eventCaptor.capture());

        AlertEvent event = eventCaptor.getValue();
        assertThat(event.roomId()).isEqualTo(ROOM_ID);
        assertThat(event.alertTitle()).isEqualTo("온도 경고");
        assertThat(event.nodeResults()).hasSize(1);
        assertThat(event.nodeResults().getFirst().nodeType()).isEqualTo(NodeType.THRESHOLD.name());
        assertThat(event.nodeResults().getFirst().value()).isEqualTo(31.5);
    }

    @Test
    @DisplayName("같은 알림 노드에 같은 history가 다시 도착하면 dedupWindowSec 동안 중복 발행하지 않음")
    void receiveSensorData_skipDuplicateAlertForSameAlertNodeAndHistory() {
        ExecutableFlow flow = createThresholdToAlertFlow();
        when(flowLoader.load(ROOM_ID)).thenReturn(List.of(flow));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("SENT"), eq(Duration.ofSeconds(DEDUP_WINDOW_SEC))))
                .thenReturn(true, false);

        String rawMessage = sensorPayload(31.5);
        listener.receiveSensorData(rawMessage);
        String rawMessage2 = sensorPayload(32.5);
        listener.receiveSensorData(rawMessage2);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations, times(2)).setIfAbsent(keyCaptor.capture(), eq("SENT"), eq(Duration.ofSeconds(DEDUP_WINDOW_SEC)));
        assertThat(keyCaptor.getAllValues().get(0)).isEqualTo(keyCaptor.getAllValues().get(1));
        assertThat(keyCaptor.getAllValues().getFirst()).startsWith("alert:dedup:node:%d:".formatted(ALERT_NODE_ID));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("test.alert.exchange"), eq("test.alert.routing-key"), org.mockito.ArgumentMatchers.any(AlertEvent.class));
    }

    private ExecutableFlow createThresholdToAlertFlow() {
        ExecutableFlow.ExecutableNode startNode = new ExecutableFlow.ExecutableNode(
                START_NODE_ID,
                "start",
                NodeType.START,
                null
        );
        ExecutableFlow.ExecutableNode thresholdNode = new ExecutableFlow.ExecutableNode(
                THRESHOLD_NODE_ID,
                "temperature threshold",
                NodeType.THRESHOLD,
                new ThresholdNodeConfig(NodeType.THRESHOLD, 0, 0, MeasurementType.TEMPERATURE, "C", Operator.GT, 30.0)
        );
        ExecutableFlow.ExecutableNode alertNode = new ExecutableFlow.ExecutableNode(
                ALERT_NODE_ID,
                "alert",
                NodeType.ALERT,
                new AlertNodeConfig(
                        NodeType.ALERT,
                        100,
                        0,
                        AlertChannel.TELEGRAM,
                        "온도 경고",
                        AlertType.COMFORT_LIMIT_EXCEEDED,
                        DEDUP_WINDOW_SEC
                )
        );

        return new ExecutableFlow(
                FLOW_ID,
                "온도 알림 플로우",
                ROOM_ID,
                List.of(),
                START_NODE_ID,
                Map.of(
                        START_NODE_ID, startNode,
                        THRESHOLD_NODE_ID, thresholdNode,
                        ALERT_NODE_ID, alertNode
                ),
                Map.of(
                        START_NODE_ID, List.of(THRESHOLD_NODE_ID),
                        THRESHOLD_NODE_ID, List.of(ALERT_NODE_ID)
                ),
                Map.of()
        );
    }

    private String sensorPayload(double temperature) {
        return """
                {
                  "roomId": %d,
                  "updatedAt": "2026-09-01T00:00:00Z",
                  "metrics": [
                    {
                      "metric": "temperature",
                      "value": %.1f,
                      "devEui": "A84041B2C3D4E5F6",
                      "updatedAt": "2026-09-01T00:00:00Z"
                    }
                  ]
                }
                """.formatted(ROOM_ID, temperature);
    }

    @Configuration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
            return mapper;
        }

        @Bean
        SensorPayloadConverter sensorPayloadConverter(ObjectMapper objectMapper) {
            return new SensorPayloadConverter(objectMapper);
        }

        @Bean
        SensorDataListener sensorDataListener(SensorPayloadConverter converter, RuleEngineHandler handler) {
            return new SensorDataListener(converter, handler);
        }

        @Bean
        RuleEngineHandler ruleEngineHandler(FlowLoader flowLoader, FlowDispatcher dispatcher) {
            return new RuleEngineHandler(flowLoader, dispatcher);
        }

        @Bean
        FlowDispatcher flowDispatcher(ExecutorService flowExecutorService, FlowScheduleFilter flowScheduleFilter, FlowExecutor flowExecutor) {
            return new FlowDispatcher(flowExecutorService, flowScheduleFilter, flowExecutor);
        }

        @Bean
        ExecutorService flowExecutorService() {
            return new DirectExecutorService();
        }

        @Bean
        FlowScheduleFilter flowScheduleFilter() {
            return new FlowScheduleFilter();
        }

        @Bean
        FlowExecutor flowExecutor(NodeExecutorRegistry nodeExecutorRegistry) {
            return new FlowExecutor(nodeExecutorRegistry);
        }

        @Bean
        NodeExecutorRegistry nodeExecutorRegistry(List<NodeExecutor> nodeExecutors) {
            return new NodeExecutorRegistry(nodeExecutors);
        }

        @Bean
        ThresholdNodeExecutor thresholdNodeExecutor() {
            return new ThresholdNodeExecutor();
        }

        @Bean
        AlertNodeExecutor alertNodeExecutor(AlertEventPublisher alertEventPublisher) {
            return new AlertNodeExecutor(alertEventPublisher);
        }

        @Bean
        AlertEventPublisher alertEventPublisher(RabbitTemplate rabbitTemplate, StringRedisTemplate redisTemplate) {
            return new AlertEventPublisher(rabbitTemplate, redisTemplate);
        }
    }

    private static class DirectExecutorService extends AbstractExecutorService {
        private boolean shutdown;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return shutdown;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }
}
