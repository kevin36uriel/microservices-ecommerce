package com.ecommerce.orderservice.service.Impl;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OutboxService;
import com.ecommerce.orderservice.service.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    //    private final WebClient.Builder webClientBuilder;
//    private final InventoryClient inventoryClient;
    private final RabbitTemplate rabbitTemplate;
    private final OutboxService outboxService;

    @Value("${order.enabled:true}")
    private boolean ordersEnabled;

    public OrderResponse fallbackMethod(OrderRequest orderRequest, String userId, Throwable throwable) {
        log.error("!!!!!!!!!!!!! Circuit breaker activado {}", throwable.getMessage());
        throw new RuntimeException("El servicio de inventario no responde. Por favor intentelo mas tarde");
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String userId, boolean isAdmin) {
        List<Order> orders;
        if (isAdmin) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByUserId(userId);
        }
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
//    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod" )
//    @Retry(name = "inventory")
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {
        if (!ordersEnabled) {
            log.warn("Pedido rechazado. Servicio desabilitado por configuración.");
            throw new RuntimeException("El servicio de pedidos esta actualmente en mantenimient. Intente mas tarder");
        }
        log.info("Colocando nuevo pedido");
        Order order = orderMapper.toOrder(orderRequest);
        order.setUserId(userId);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PLACED);

        Order savedOrder = orderRepository.save(order);
        log.info("Guardado con exito. ID: {}", savedOrder.getId());

        List<OrderPlacedEvent.OrderItemEvent> orderItems =
                order.getOrderLineItemsList().stream()
                        .map(item -> new OrderPlacedEvent.OrderItemEvent(
                                item.getSku(), item.getPrice().toString(), item.getQuantity()
                        )).toList();

        OrderPlacedEvent event = new OrderPlacedEvent(savedOrder.getOrderNumber(), orderRequest.getEmail(), orderItems);

        boolean sentToRabbit = false;

        try {
            rabbitTemplate.convertAndSend("order-events", "order.placed", event);
            sentToRabbit = true;
            log.info("Enviando pedido con exito");
        } catch (AmqpException e) {
            log.warn("Rabbit caído, el evento quedará pendiente en outbox");
        }

        outboxService.saveOrderPlaceEvent(event, sentToRabbit);

        log.info("Enviando pedido a RabbitMQ. NumberOrder: {}", savedOrder.getOrderNumber());

        return orderMapper.toOrderResponse(savedOrder);

    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        orderRepository.deleteById(id);
        log.info("Eliminado con exito. ID: {}", id);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderNumber, OrderStatus newStatus) {
        log.info("Actualizando en la base de datos: Orden: {}", orderNumber);
        Optional<Order> order = orderRepository.findByOrderNumber(orderNumber);
        if (order.isPresent()) {
            Order orderToUpdate = order.get();
            orderToUpdate.setStatus(newStatus);
            orderRepository.save(orderToUpdate);
        } else {
            log.error("No existe el registro con el id: {}", orderNumber);
            throw new ResourceNotFoundException("Order", "orderNumber", orderNumber);
        }
    }
}
