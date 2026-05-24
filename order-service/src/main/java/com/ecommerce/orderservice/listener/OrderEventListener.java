package com.ecommerce.orderservice.listener;

import com.ecommerce.orderservice.event.OrderCancelledEvent;
import com.ecommerce.orderservice.event.OrderConfirmedEvent;
import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderService orderService;

    @RabbitListener(queues = "inventory-confirmed-queue")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        if(event.orderNumber() == null) {
            log.error("OrderConfirmedEvent: orderNumber={} no encontrado", event.orderNumber());
            return;
        }
        orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CONFIRMED);
    }

    @RabbitListener(queues = "inventory-cancelled-queue")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if(event.orderNumber() == null) {
            log.error("OrderCancelledEvent: orderNumber={} no encontrado", event.orderNumber());
            return;
        }
        orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CANCELLED);
    }
}
