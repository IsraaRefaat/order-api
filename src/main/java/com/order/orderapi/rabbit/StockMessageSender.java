package com.order.orderapi.rabbit;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockMessageSender {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.store.routingkey}")
    private String routingKey;

    public void sendConsumeStockMessage(Long orderId, Map<Long, Integer> products_quantities){
        ConsumeStockMessage message = new ConsumeStockMessage(orderId, products_quantities);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);

        System.out.println("Sent stock consume message: " + message);
    }
}
