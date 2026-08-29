package com.skipcart.orderservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${skipcart.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${skipcart.rabbitmq.order-created-routing-key}")
    private String orderCreatedRoutingKey;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(exchangeName, orderCreatedRoutingKey, event);
        log.info("OrderCreatedEvent published successfully");
    }
}