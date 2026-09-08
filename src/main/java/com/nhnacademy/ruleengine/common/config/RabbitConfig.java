package com.nhnacademy.ruleengine.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.queue.name}")
    private String queueName;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;
    @Value("${rabbitmq.dlq.exchange}")
    private String dlxExchangeName;
    @Value("${rabbitmq.dlq.name}")
    private String dlqName;
    @Value("${rabbitmq.dlq.routing-key}")
    private String dlqRoutingKey;

    /**
     * JSON 직렬화/역직렬화를 위한 Jackson ObjectMapper 설정 빈
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // DTO에 정의되지 않은 필드가 JSON에 포함되어 있으면 무시
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);   // JSON 직렬화 시 null 필드 제외
        return mapper;
    }

    /**
     * Spring AMQP에서 Jackson ObjectMapper를 기반으로 메시지를 JSON 형태로 변환하는 컨버터입니다.
     */
    private Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitMQ로 메시지를 발행(Publish)할 때 사용하는 메인 템플릿 빈입니다.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter(objectMapper));
        return rabbitTemplate;
    }

    //센서 데이터를 수신받을 메인 Topic Exchange 생성 빈
    @Bean
    public TopicExchange sensorTopicExchange() {
        return new TopicExchange(exchangeName);
    }

    //센서 데이터를 저장할 메인 Queue 생성 빈
    @Bean
    public Queue sensorQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    //센서 Queue와 Topic Exchange를 지정한 라우팅 키로 연결(Binding)하는 빈
    @Bean
    public Binding sensorBinding(Queue sensorQueue, TopicExchange sensorTopicExchange) {
        return BindingBuilder.bind(sensorQueue)
                .to(sensorTopicExchange)
                .with(routingKey);
    }


    //처리 실패한 메시지를 전달받는 Direct 형태의 Dead Letter Exchange(DLX) 생성 빈
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName);
    }

    //최종적으로 처리 실패 메시지를 보관할 Dead Letter Queue(DLQ) 생성 빈
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqName).build();
    }

    //DLQ와 DLX를 DLQ 전용 라우팅 키로 연결(Binding)하는 빈
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(dlqRoutingKey);
    }
}
