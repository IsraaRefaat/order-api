package com.order.orderapi.dto;

import lombok.Data;

@Data
public class CouponConsumeRequest {
    private String couponCode;
    private String customerId;
    private String orderId;
    
    public CouponConsumeRequest(String couponCode, String customerId, String orderId) {
        this.couponCode = couponCode;
        this.customerId = customerId;
        this.orderId = orderId;
    }
}