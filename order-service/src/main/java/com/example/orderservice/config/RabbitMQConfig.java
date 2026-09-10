package com.example.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE    = "order.exchange";
    public static final String QUEUE       = "order.queue";
    public static final String DEBUG_QUEUE = "order.debug.queue";
    public static final String ROUTING_KEY = "order.created";

    // 1. Tạo Exchange kiểu Direct
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE);
    }

    // 2. Queue chính dành cho notification-service (Tự động gửi mail)
    @Bean
    public Queue orderQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(orderQueue)
                .to(orderExchange)
                .with(ROUTING_KEY);
    }

    // 3. Queue RIÊNG DÀNH CHO BẠN DEBUG (Không có ai tiêu thụ, tin nhắn nằm im trên Web RabbitMQ)
    @Bean
    public Queue orderDebugQueue() {
        return new Queue(DEBUG_QUEUE, true);
    }

    @Bean
    public Binding orderDebugBinding(Queue orderDebugQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(orderDebugQueue)
                .to(orderExchange)
                .with(ROUTING_KEY);
    }

    // 4. Cấu hình JSON Serializer để gửi/nhận Object thay vì String
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 5. Cấu hình RabbitTemplate dùng JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
