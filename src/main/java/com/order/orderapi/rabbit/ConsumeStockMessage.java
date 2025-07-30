package com.order.orderapi.rabbit;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ConsumeStockMessage {

    private Long orderId;
    private Map<Long, Integer> products_quantities;
}
