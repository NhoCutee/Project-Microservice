package com.example.orderservice.service.impl;

import com.example.orderservice.client.UserClient;
import com.example.orderservice.config.KafkaTopicConfig;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.dto.UserDto;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Override
    @CacheEvict(value = "allItem", allEntries = true)
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        Order order = Order.builder()
                .userId(requestDTO.getUserId())
                .product(requestDTO.getProduct())
                .price(requestDTO.getPrice())
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .product(savedOrder.getProduct())
                .price(savedOrder.getPrice())
                .build();
        kafkaTemplate.send("order-topic", event);
        System.out.println("Da gui Kafka event: " + event);
        return mapToResponseDTO(savedOrder);
    }

    @Override
    @Cacheable(value = "allItem", key = "'all'")
    public List<OrderResponseDTO> getAllOrders() {
        System.out.println("Querying DB.....");
        return orderRepository.findAll()
                .stream()
                .map(order -> {
                    UserDto userDto;
                    try {
                        userDto = userClient.getUserById(order.getUserId());
                    } catch (Exception e) {
                        userDto = new UserDto(order.getUserId(), "N/A", "N/A");
                    }

                    return OrderResponseDTO.builder()
                            .id(order.getId())
                            .product(order.getProduct())
                            .price(order.getPrice())
                            .user(userDto)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "item", key = "#id")
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToResponseDTO(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        return orderRepository
                .findByUserId(userId)
                .stream()
                .map(order -> OrderResponseDTO.builder()
                        .id(order.getId())
                        .product(order.getProduct())
                        .price(order.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        UserDto userDto;
        try {
            userDto = userClient.getUserById(order.getUserId());
        } catch (Exception e) {
            userDto = new UserDto(order.getUserId(), "N/A", "N/A");
        }
        return OrderResponseDTO.builder()
                .id(order.getId())
                .product(order.getProduct())
                .price(order.getPrice())
                .user(userDto)
                .build();
    }
}
