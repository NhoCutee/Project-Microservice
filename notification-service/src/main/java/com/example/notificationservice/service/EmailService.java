package com.example.notificationservice.service;

import com.example.notificationservice.event.OrderCreatedEvent;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendOrderEmail(OrderCreatedEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(fromEmail);
            helper.setSubject("Don hang moi #" + event.getOrderId());

            String body = "<p>Xin chao userId <b>" + event.getUserId() + "</b>,</p>" +
                    "<p>Don hang #" + event.getOrderId() + " vua duoc tao thanh cong!</p>" +
                    "<p>Tong tien: <b>$" + event.getPrice() + "</b></p>";

            helper.setText(body, true);

            mailSender.send(message);
            System.out.println("Gui email thanh cong toi: " + fromEmail);

        } catch (Exception e) {
            System.err.println("Gui email that bai: " + e.getMessage());
        }
    }
}
