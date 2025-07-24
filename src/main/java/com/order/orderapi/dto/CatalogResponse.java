package com.order.orderapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponse<T> {
    private boolean success;
    private String message;
    private int recordsCount;
    private T data;
}