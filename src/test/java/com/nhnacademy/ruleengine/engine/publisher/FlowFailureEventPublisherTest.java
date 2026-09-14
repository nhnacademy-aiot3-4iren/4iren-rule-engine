package com.nhnacademy.ruleengine.engine.publisher;

import com.nhnacademy.ruleengine.engine.model.FlowFailureEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlowFailureEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private FlowFailureEventPublisher publisher;

    private static final String EXCHANGE = "test.flow-failure.exchange";
    private static final String ROUTING_KEY = "test.flow-failure";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "flowFailureExchange", EXCHANGE);
        ReflectionTestUtils.setField(publisher, "flowFailureRoutingKey", ROUTING_KEY);
    }

    @Test
    @DisplayName("플로우 실패 이벤트를 지정된 exchange와 routing key로 발행한다")
    void publish_success() {
        FlowFailureEvent event = event();

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }

    @Test
    @DisplayName("플로우 실패 이벤트 발행 실패는 밖으로 전파하지 않는다")
    void publish_doesNotThrowWhenRabbitMqFails() {
        FlowFailureEvent event = event();
        doThrow(new RuntimeException("rabbit failed"))
                .when(rabbitTemplate)
                .convertAndSend(EXCHANGE, ROUTING_KEY, event);

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }

    private FlowFailureEvent event() {
        return new FlowFailureEvent(
                1L,
                10L,
                "flow",
                Instant.parse("2026-09-13T00:00:00Z"),
                Instant.parse("2026-09-13T00:00:01Z"),
                RuntimeException.class.getName(),
                "boom"
        );
    }
}
