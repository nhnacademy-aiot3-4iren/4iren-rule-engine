package com.nhnacademy.ruleengine.engine.listener;

import com.nhnacademy.ruleengine.engine.converter.SensorPayloadConverter;
import com.nhnacademy.ruleengine.engine.model.SensorPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorDataListener {

    private final SensorPayloadConverter converter;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveSensorData(String rawMessage) {
        log.debug("RabbitMQ 메시지 수신: {}", rawMessage);

        SensorPayload payload = converter.convert(rawMessage);

        log.info("센서 데이터 변환 완료 - RoomID: {}, Device: {}, Metrics Count: {}",
                payload.device().roomId(),
                payload.device().deviceName(),
                payload.sensorDataList().size());
    }
}
