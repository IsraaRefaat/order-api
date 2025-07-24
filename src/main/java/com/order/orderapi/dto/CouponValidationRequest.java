package com.order.orderapi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponValidationRequest {
    private String couponCode;
    private BigDecimal orderTotal;
    private String customerId;
    
    public CouponValidationRequest(String couponCode, BigDecimal orderTotal, String customerId) {
        this.couponCode = couponCode;
        this.orderTotal = orderTotal;
        this.customerId = customerId;
    }
}