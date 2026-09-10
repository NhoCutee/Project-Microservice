package com.example.notificationservice.service;

import com.example.notificationservice.config.RabbitMQConfig;
import com.example.notificationservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    // Tu dong lay tin nhan tu order.queue va gui mail ngay lap tuc!
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("=================================================");
        System.out.println("[NOTIFICATION SERVICE] Nhan su kien tu RabbitMQ:");
        System.out.println("   -> Ma Don Hang: " + event.getOrderId());
        System.out.println("   -> Ma Nguoi Dung: " + event.getUserId());
        System.out.println("   -> San Pham: " + event.getProduct());
        System.out.println("   -> Gia Tien: " + event.getPrice());
        emailService.sendOrderEmail(event);
        System.out.println("   -> Email thong bao da duoc gui thanh cong!");
        System.out.println("=================================================");
    }
}
