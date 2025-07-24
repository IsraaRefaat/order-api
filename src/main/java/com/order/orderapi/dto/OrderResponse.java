package com.order.orderapi.dto;

import com.order.orderapi.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String customerEmail;
    private String customerName;
    private BigDecimal orderTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String appliedCouponCode;
    private OrderStatus status;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}