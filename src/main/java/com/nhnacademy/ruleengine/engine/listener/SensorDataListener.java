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
import java.util.Optional;

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

        Optional<EnvironmentContext> converted = converter.convertIfAssigned(rawMessage);
        if (converted.isEmpty()) {
            return;
        }

        EnvironmentContext payload = converted.get();

        log.info("센서 데이터 변환 완료 roomId={}, 측정값수={}",
                payload.roomId(),
                payload.metrics().size());

        handler.process(payload).join();
        //비동기로 동작하는 룰 엔진처리가 모두 완료될 때까지 rabbitMQ 리스너 스레드 블로킹
        // retry는 “비동기 플로우까지 끝난 뒤 실패했는지”를 기준으로 동작
    }
}
