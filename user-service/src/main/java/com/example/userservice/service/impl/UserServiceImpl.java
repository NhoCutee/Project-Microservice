package com.example.userservice.service.impl;

import com.example.userservice.client.OrderClient;
import com.example.userservice.dto.OrderDto;
import com.example.userservice.dto.UserRequestDTO;
import com.example.userservice.dto.UserResponseDTO;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrderClient orderClient;

    @Override
    @CacheEvict(value = "allUser", allEntries = true)
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        User user = User.builder()
                .name(requestDTO.getName())
                .email(requestDTO.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }

    @Override
    @Cacheable(value = "allUser", key = "'all'")
    public List<UserResponseDTO> getAllUsers() {
        System.out.println("Querying DB.....");
        return userRepository.findAll()
                .stream()
                .map(user -> UserResponseDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToResponseDTO(user);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        List<OrderDto> orderDto;
        try{
            orderDto = orderClient.getOrderByUserId(user.getId());
        }catch (Exception e){
            orderDto = Collections.emptyList();
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .order(orderDto)
                .build();
    }

}
