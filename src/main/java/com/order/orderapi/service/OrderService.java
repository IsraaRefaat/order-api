package com.order.orderapi.service;

import com.order.orderapi.dto.*;
import com.order.orderapi.entity.Order;
import com.order.orderapi.entity.OrderItem;
import com.order.orderapi.entity.OrderStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface OrderService {

     OrderResponse createOrder(OrderCreateRequest request, String authToken);

     OrderResponse getOrder(Long orderId);

     List<OrderResponse> getOrdersByCustomer(String customerEmail);

     List<OrderResponse> getAllOrders();
}
