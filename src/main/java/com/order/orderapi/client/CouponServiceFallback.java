package com.order.orderapi.client;

import com.order.orderapi.dto.CouponConsumeRequest;
import com.order.orderapi.dto.CouponValidationRequest;
import com.order.orderapi.dto.CouponValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CouponServiceFallback implements CouponServiceClient {
    
    @Override
    public CouponValidationResponse validateCoupon(CouponValidationRequest request) {
        log.error("Coupon service is unavailable, returning fallback response for coupon: {}", request.getCouponCode());
        CouponValidationResponse response = new CouponValidationResponse();
        response.setValid(false);
        response.setCouponCode(request.getCouponCode());
        response.setMessage("Coupon service unavailable");
        return response;
    }
    
    @Override
    public void consumeCoupon(CouponConsumeRequest request) {
        log.error("Coupon service is unavailable, cannot consume coupon: {}", request.getCouponCode());
        throw new RuntimeException("Coupon service unavailable - cannot consume coupon");
    }
}