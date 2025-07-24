package com.order.orderapi.client;

import com.order.orderapi.dto.CatalogResponse;
import com.order.orderapi.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CategoryServiceFallback implements CategoryServiceClient {
    
    @Override
    public CatalogResponse<ProductDto> getProductById(Long id) {
        log.error("Category service is unavailable for product retrieval: {}", id);
        throw new RuntimeException("Product service is currently unavailable. Please try again later.");
    }
}