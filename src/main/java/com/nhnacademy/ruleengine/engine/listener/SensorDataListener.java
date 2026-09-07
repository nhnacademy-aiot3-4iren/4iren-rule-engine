package com.nhnacademy.ruleengine.engine.listener;

import com.nhnacademy.ruleengine.engine.converter.SensorPayloadConverter;
import com.nhnacademy.ruleengine.engine.handler.RuleEngineHandler;
import com.nhnacademy.ruleengine.engine.model.EnvironmentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorDataListener {

    private final SensorPayloadConverter converter;
    private final RuleEngineHandler handler;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveSensorData(Message message) {
        log.info("RabbitMQ 메시지 수신: {}", message);

        String rawMessage = new String(message.getBody(), StandardCharsets.UTF_8);

        EnvironmentContext payload = converter.convert(rawMessage);

        log.info("센서 데이터 변환 완료 roomId={}, 측정값수={}",
                payload.roomId(),
                payload.metrics().size());

        handler.process(payload);
    }
}
