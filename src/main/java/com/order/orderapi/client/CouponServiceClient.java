package com.order.orderapi.client;

import com.order.orderapi.dto.CouponConsumeRequest;
import com.order.orderapi.dto.CouponValidationRequest;
import com.order.orderapi.dto.CouponValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "coupon-service", url = "${coupon.service.url}", fallback = CouponServiceFallback.class)
public interface CouponServiceClient {
    
    @PostMapping("/api/coupons/validate")
    CouponValidationResponse validateCoupon(@RequestBody CouponValidationRequest request);
    
    @PostMapping("/api/coupons/consume")
    void consumeCoupon(@RequestBody CouponConsumeRequest request);
}