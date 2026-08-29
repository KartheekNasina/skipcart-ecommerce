package com.skipcart.orderservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @RabbitListener(queues = "${skipcart.rabbitmq.order-created-queue}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("📩 Received OrderCreatedEvent: orderId={}, userId={}, total={}",
                event.getOrderId(), event.getUserId(), event.getTotalAmount());

        // Simulating what a real consumer might do:
        log.info("Would send order confirmation to userId {} for order #{} (total: {})",
                event.getUserId(), event.getOrderId(), event.getTotalAmount());
    }
}