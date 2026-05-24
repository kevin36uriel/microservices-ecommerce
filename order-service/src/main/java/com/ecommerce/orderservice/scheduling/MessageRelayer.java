package com.ecommerce.orderservice.scheduling;

import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OutboxEvents;
import com.ecommerce.orderservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageRelayer {
    private final RabbitTemplate rabbitTemplate;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 10000)
    public void relayMessages(){
        List<OutboxEvents> pendingEvents = outboxService.getPendingEvents();
        if(!pendingEvents.isEmpty()){
            log.info("Relayer: Detectados {} eventos pendientes", pendingEvents.size());
            for(OutboxEvents events : pendingEvents){
                try {
                    OrderPlacedEvent orginalEvents = objectMapper.readValue(
                            events.getPayload(), OrderPlacedEvent.class
                    );
                    rabbitTemplate.convertAndSend("order-events", "order.placed",  orginalEvents);
                    outboxService.markAsProcessed(events.getId());
                    log.info("Evento con ID {} enviado y marcado como procesado", events.getId());
                } catch (JacksonException | AmqpException e) {
                    log.error("Error al procesar el evento con ID {}: {}", events.getId(), e.getMessage());
                }
            }
        }
    }
}
