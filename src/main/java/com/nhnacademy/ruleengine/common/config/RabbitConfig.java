package com.nhnacademy.ruleengine.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
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
    @Value("${rabbitmq.listener.retry.max-attempts:3}")
    private int retryMaxAttempts;//재시도 최대 횟수
    @Value("${rabbitmq.listener.retry.initial-interval:1000}")
    private long retryInitialInterval;//첫 재시도까지의 대기 시간
    @Value("${rabbitmq.listener.retry.multiplier:2.0}")
    private double retryMultiplier;//재시도 대기 시간 증가 배수
    @Value("${rabbitmq.listener.retry.max-interval:10000}")
    private long retryMaxInterval;//재시도 대기 시간의 최대 한도

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

    /**
     * @RabbitListener 어노테이션이 붙은 리스너 컨테이너 동작을 관리하는 팩토리 빈입니다.
     * 메시지 수신 시 적용될 커스텀 컨버터나 재시도 인터셉터를 지정합니다.
     */
    //
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Advice retryInterceptor
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        //RabbitMQ 연결을 위한 ConnectionFactory 설정
        factory.setConnectionFactory(connectionFactory);

        //예외 발생 후 거절된 메시지를 기존 큐료 재배치(requeue)하지 않도록 설정
        //Interceptor가 잡지 못한 심각한 예외 발생 시 또는 Requeue 여부의 기본 프레임워크 정책으로 동작
        factory.setDefaultRequeueRejected(false);

        //메시지 처리시 적용할 부가 기능(재시도 정책) 등록
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    /**
     * 메시지 소비(Consume) 실패 시 재시도 방식 및 최종 복구 처리 전략을 설정하는 인터셉터 빈입니다.
     */
    @Bean
    public Advice retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(retryMaxAttempts)//재시도 횟수 지정
                .backOffOptions(retryInitialInterval, retryMultiplier, retryMaxInterval)//재시도 대기시간 지정
                //최대 재시도 횟수 실패 시: 메시지를 거부(reject)하고 requeue하지 않음 -> 큐에 x-dead-letter-exchange가 설정되어 있다면 DLQ로 라우팅
                //Retry 기능이 정상 작동하여 최대 재시도 횟수를 모두 초과한 직후 동작
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public TopicExchange sensorTopicExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue sensorQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Binding sensorBinding(Queue sensorQueue, TopicExchange sensorTopicExchange) {
        return BindingBuilder.bind(sensorQueue)
                .to(sensorTopicExchange)
                .with(routingKey);
    }


    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(dlqRoutingKey);
    }
}
