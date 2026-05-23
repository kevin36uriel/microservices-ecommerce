package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse getOrderById(Long id);
    //List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrders(String userId, boolean isAdmin);
    OrderResponse placeOrder(OrderRequest orderRequest, String userId);
    void deleteOrder(Long id);
    void updateOrderStatus(String orderNumber, OrderStatus orderStatus);
}
