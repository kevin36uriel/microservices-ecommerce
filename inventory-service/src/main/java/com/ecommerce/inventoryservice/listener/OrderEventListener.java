package com.ecommerce.inventoryservice.listener;

import com.ecommerce.inventoryservice.event.OrderPlacedEvent;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class OrderEventListener {
    private final InventoryService inventoryService;

    @RabbitListener(queues = "inventory-queue")
    public void handlerOrderPlacedEvent(OrderPlacedEvent event){
        log.info("Evento recibido para la orden {}", event.orderNumber());
        event.items().forEach(item -> {
            try{
                inventoryService.reduceStock(item.sku(), item.quantity());
                log.info("Stock descontado {}: ", item.sku());
            }catch (Exception e){
                log.error("Error al descontar el stock del {}", item.sku());
            }
        });
    }
}
