package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.model.OutboxEvents;

import java.util.List;

public interface OutboxService {
    void saveOrderPlaceEvent(OrderPlacedEvent orderPlacedEvent, boolean isProcessed);
    List<OutboxEvents> getPendingEvents();
    void markAsProcessed(Long id);
}
