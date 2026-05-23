package com.ecommerce.orderservice.event;

public record OrderCancelledEvent(String orderNumber, String email, String reason) {
}
