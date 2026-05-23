package com.ecommerce.inventoryservice.event;

public record OrderCancelledEvent(String orderNumber, String email, String reason) {
}
