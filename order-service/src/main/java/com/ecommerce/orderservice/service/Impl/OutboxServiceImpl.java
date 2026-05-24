package com.ecommerce.orderservice.service.Impl;

import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OutboxEvents;
import com.ecommerce.orderservice.repository.OutboxEventsRepository;
import com.ecommerce.orderservice.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {
    private final OutboxEventsRepository outboxEventsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveOrderPlaceEvent(OrderPlacedEvent events, boolean isProcessed) {
        try{
            String payload = objectMapper.writeValueAsString(events);

            OutboxEvents outboxEvents = OutboxEvents.builder()
                    .aggregateId(events.orderNumber())
                    .type("ORDER_PLACED")
                    .payload(payload)
                    .createAt(LocalDateTime.now())
                    .processed(isProcessed)
                    .build();
            outboxEventsRepository.save(outboxEvents);

        }catch (JsonProcessingException ex){
            log.error(ex.getMessage());
        }
    }

    @Override
    public List<OutboxEvents> getPendingEvents() {
        return outboxEventsRepository.findByProcessedFalse();
    }

    @Override
    public void markAsProcessed(Long id) {
        outboxEventsRepository.findById(id).ifPresent(event -> {
            event.setProcessed(true);
            outboxEventsRepository.save(event);
            log.info("Marcado como procesado");
        });
    }
}
