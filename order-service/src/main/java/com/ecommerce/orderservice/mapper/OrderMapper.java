package com.ecommerce.orderservice.mapper;

import com.ecommerce.orderservice.dto.OrderLineItemsRequest;
import com.ecommerce.orderservice.dto.OrderLineItemsResponse;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderLineItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order  toOrder(OrderRequest orderRequest);

    OrderLineItems toOrderLineItems(OrderLineItemsRequest orderLineItemsRequest);

    OrderResponse toOrderResponse(Order order);

    OrderLineItemsResponse toOrderLineItemsResponse(OrderLineItems orderLineItems);
}
