package com.order.orderapi.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderEvent {
    private Long orderId;
    private String customerEmail;
    private String customerName;
    private String couponCode;
    private BigDecimal price;
    private Long transactionId;
}
