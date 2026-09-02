package com.skipcart.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${skipcart.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${skipcart.rabbitmq.notification-queue}")
    private String notificationQueueName;

    @Value("${skipcart.rabbitmq.order-created-routing-key}")
    private String orderCreatedRoutingKey;

    // Declares the SAME exchange (idempotent - RabbitMQ won't duplicate it,
    // just confirms it exists with matching properties)
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(exchangeName);
    }

    // OWN queue, separate from order-service's queue
    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueueName, true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(orderExchange)
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}