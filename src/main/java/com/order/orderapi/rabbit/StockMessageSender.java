package com.order.orderapi.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockMessageSender {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.store.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.store.routingkey}")
    private String routingKey;

    public void sendConsumeStockMessage(Map<Long, Integer> products_quantities){
        ConsumeStockMessage message = new ConsumeStockMessage(products_quantities);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);

        System.out.println("Sent stock consume message: " + message);
    }
}
