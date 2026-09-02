package com.skipcart.notificationservice.listener;

import com.skipcart.notificationservice.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationListener {

    @RabbitListener(queues = "${skipcart.rabbitmq.notification-queue}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("[NOTIFICATION-SERVICE] Received OrderCreatedEvent: orderId={}, userId={}",
                event.getOrderId(), event.getUserId());

        sendOrderConfirmationEmail(event);
    }

    private void sendOrderConfirmationEmail(OrderCreatedEvent event) {
        // Simulating email send - real email integration comes later
        log.info("[NOTIFICATION-SERVICE] 📧 Sending order confirmation email to userId {} " +
                        "for order #{} - Total: {}, Shipping to: {}",
                event.getUserId(), event.getOrderId(), event.getTotalAmount(), event.getShippingAddress());
    }
}