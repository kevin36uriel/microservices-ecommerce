package com.ecommerce.orderservice.service.Impl;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.exception.ResourceNotFoundException;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
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
        if(isAdmin) {
            orders = orderRepository.findAll();
        }else{
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
            if(!ordersEnabled) {
                log.warn("Pedido rechazado. Servicio desabilitado por configuración.");
                throw new RuntimeException("El servicio de pedidos esta actualmente en mantenimient. Intente mas tarder");
            }
            log.info("Colocando nuevo pedido");
            Order order = orderMapper.toOrder(orderRequest);
            order.setUserId(userId);
//            for(var item : order.getOrderLineItemsList()){
//                String sku = item.getSku();
//                Integer quantity = item.getQuantity();
//                try{
////                webClientBuilder.build()
////                        .put()
////                        .uri("http://localhost:8083/api/v1/inventory/reduce/" + sku,
////                                uriBuilder -> uriBuilder.queryParam("quantity", quantity).build())
////                        .retrieve()
////                        .bodyToMono(String.class)
////                        .block();
//                    inventoryClient.reduceStock(sku, quantity);
//                }catch (Exception e){
//                    log.error("Error al reducir stock para el producto {}: {}", sku, e.getMessage());
//                    throw new IllegalArgumentException("No se pudo procesar el pedido: Stock insuficiente o error de inventario");
//                }
//            }
            order.setOrderNumber(UUID.randomUUID().toString());

            Order savedOrder = orderRepository.save(order);
            log.info("Guardado con exito. ID: {}", savedOrder.getId());

            List<OrderPlacedEvent.OrderItemEvent> orderItems =
                    order.getOrderLineItemsList().stream()
                            .map(item -> new OrderPlacedEvent.OrderItemEvent(
                                    item.getSku(), item.getPrice().toString(), item.getQuantity()
                            )).toList();

            OrderPlacedEvent event = new OrderPlacedEvent(
                    savedOrder.getOrderNumber(), orderRequest.getEmail(), orderItems
            );
            rabbitTemplate.convertAndSend("order-events", "order.placed", event);
            log.info("Enviando pedido a RabbitMQ. NumberOrder: {}", savedOrder.getOrderNumber());

            return orderMapper.toOrderResponse(savedOrder);

    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if(!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        orderRepository.deleteById(id);
        log.info("Eliminado con exito. ID: {}", id);
    }
}
