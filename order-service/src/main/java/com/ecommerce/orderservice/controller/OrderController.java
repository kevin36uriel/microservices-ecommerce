package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest orderRequest, @AuthenticationPrincipal Jwt jwt) {
        return orderService.placeOrder(orderRequest, jwt.getSubject());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> getOrder(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        boolean isAdmin = false;

        Map<String, Object> realmAcces = jwt.getClaim("realm_access");

        if(realmAcces != null && realmAcces.containsKey("roles")) {
            List<String> roles = (List<String>) realmAcces.get("roles");
            isAdmin = roles.stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"));
        }

        return orderService.getOrders(userId, isAdmin);

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@Valid @PathVariable Long id){
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderById(@Valid @PathVariable Long id){
        orderService.deleteOrder(id);
    }
}
