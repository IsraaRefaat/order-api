package com.order.orderapi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponValidationResponse {
    private boolean valid;
    private String couponCode;
    private BigDecimal discountAmount;
    private String discountType;
    private String message;
}