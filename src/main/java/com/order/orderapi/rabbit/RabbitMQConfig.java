package com.order.orderapi.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.response.queue}")
    private String responseQueue;

    @Value("${spring.rabbitmq.response.routingkey}")
    private String responseRoutingKey;

    // ---- Notification Queue Config ----
    public static final String NOTIFICATION_QUEUE = "order-notification-queue";
    public static final String NOTIFICATION_EXCHANGE = "order-notification-exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "order.notification.key";

    @Bean
    public TopicExchange stockExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(responseQueue, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding responseBinding(Queue responseQueue, TopicExchange stockExchange) {
        return BindingBuilder.bind(responseQueue)
                .to(stockExchange)
                .with(responseRoutingKey);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
