package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"orderId","product","price", "user"})
public class OrderResponseDTO implements Serializable {

    @JsonProperty("orderId")
    private Long id;

    private String product;
    private Double price;

    private UserDto user;
}
