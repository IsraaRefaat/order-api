package com.order.orderapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${user.service.url}", fallback = UserServiceFallback.class)
public interface UserServiceClient {
    
    @GetMapping("/users/check-role")
    Boolean checkUserRole(@RequestHeader("Authorization") String token,
                          @RequestParam("role") String role);
}