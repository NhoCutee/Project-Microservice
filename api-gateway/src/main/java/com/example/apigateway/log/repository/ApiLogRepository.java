package com.example.apigateway.log.repository;

import com.example.apigateway.log.entity.ApiLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogRepository extends ReactiveCrudRepository<ApiLog, String> {
}
