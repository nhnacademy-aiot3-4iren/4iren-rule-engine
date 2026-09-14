package com.nhnacademy.ruleengine.engine.publisher;

import com.nhnacademy.ruleengine.engine.model.FlowFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlowFailureEventPublisher {

    @Value("${rabbitmq.flow-failure.exchange}")
    private String flowFailureExchange;

    @Value("${rabbitmq.flow-failure.routing-key}")
    private String flowFailureRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public void publish(FlowFailureEvent event) {
        try {
            rabbitTemplate.convertAndSend(flowFailureExchange, flowFailureRoutingKey, event);
            log.debug("플로우 실패 이벤트 발행 완료 roomId={}, flowId={}", event.roomId(), event.flowId());
        } catch (Exception e) {
            log.error("플로우 실패 이벤트 발행 실패 roomId={}, flowId={}", event.roomId(), event.flowId(), e);
        }
    }
}
