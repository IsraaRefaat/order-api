package com.order.orderapi.rabbit;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StockResponse {

    private Long orderId;
    private boolean success;
    private String message;
    private Map<String, Integer> insufficientProducts;
}
