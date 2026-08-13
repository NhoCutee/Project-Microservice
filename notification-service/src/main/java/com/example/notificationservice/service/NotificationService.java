package com.example.notificationservice.service;

import com.example.notificationservice.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(
            topics = "order-topic",
            groupId = "notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("=================================================");
        System.out.println("[NOTIFICATION SERVICE] Nhận sự kiện từ Kafka:");
        System.out.println("   -> Mã Đơn Hàng: " + event.getOrderId());
        System.out.println("   -> Mã Người Dùng: " + event.getUserId());
        System.out.println("   -> Sản Phẩm: " + event.getProduct());
        System.out.println("   -> Giá Tiền: " + event.getPrice());
        System.out.println("   -> Tự động gửi Email/SMS thông báo thành công!");
        System.out.println("=================================================");
    }
}
