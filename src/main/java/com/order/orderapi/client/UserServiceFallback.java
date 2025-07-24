package com.order.orderapi.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceFallback implements UserServiceClient {
    
    @Override
    public Boolean checkUserRole(String token, String role) {
        log.error("User service is unavailable for role check");
        throw new RuntimeException("User authentication service is currently unavailable");
    }
}