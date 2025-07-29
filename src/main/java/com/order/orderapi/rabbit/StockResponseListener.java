package com.order.orderapi.rabbit;

import com.order.orderapi.entity.Order;
import com.order.orderapi.entity.OrderStatus;
import com.order.orderapi.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockResponseListener {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = "${spring.rabbitmq.response.queue}")
    public void handleStockResponse(StockResponse stockResponse){

        if(!stockResponse.isSuccess()){

            Order order = orderRepository.findById(stockResponse.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            log.info(stockResponse.toString());
        }
    }
}
