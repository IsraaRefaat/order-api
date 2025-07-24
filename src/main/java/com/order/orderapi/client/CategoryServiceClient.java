package com.order.orderapi.client;

import com.order.orderapi.dto.CatalogResponse;
import com.order.orderapi.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}", fallback = CategoryServiceFallback.class)
public interface CategoryServiceClient {
    
    @GetMapping("/products/id/{id}")
    CatalogResponse<ProductDto> getProductById(@PathVariable("id") Long id);
}