package com.example.userservice.client;

import com.example.userservice.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/user/{userId}")
    List<OrderDto> getOrderByUserId(@PathVariable("userId") Long userId);
}