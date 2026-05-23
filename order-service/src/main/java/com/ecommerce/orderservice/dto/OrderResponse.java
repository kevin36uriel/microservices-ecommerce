package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private List<OrderLineItemsResponse> orderLineItemsList;
    private OrderStatus status;
}
