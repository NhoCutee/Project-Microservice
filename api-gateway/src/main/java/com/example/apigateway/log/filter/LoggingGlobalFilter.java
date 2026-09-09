package com.example.apigateway.log.filter;

import com.example.apigateway.log.entity.ApiLog;
import com.example.apigateway.log.repository.ApiLogRepository;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private final ApiLogRepository apiLogRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String traceId   = UUID.randomUUID().toString();
        String method    = request.getMethod().name();
        String uri       = request.getURI().getPath();
        String params    = request.getURI().getQuery();
        String originIp  = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress() : "";
        String userId    = request.getHeaders().getFirst("X-User-Id");
        String resource  = extractResource(uri);
        String action    = resolveAction(method);
        String headers   = request.getHeaders().toSingleValueMap().toString();
        long   timeStamp = System.currentTimeMillis() / 1000;
        String serverPort = String.valueOf(request.getURI().getPort());

        // ===== Lưu REQUEST log =====
        ApiLog requestLog = ApiLog.builder()
                .requestId(UUID.randomUUID().toString())
                .traceId(traceId)
                .type("REQUEST")
                .method(method)
                .uri(uri)
                .resource(resource)
                .action(action)
                .originIp(originIp)
                .userId(userId)
                .httpCode(0)
                .logLevel("INFO")
                .logTime(LocalDateTime.now())
                .timeStamp(timeStamp)
                .params(params)
                .header(headers)
                .serverPort(serverPort)
                .isNew(true)
                .build();

        // ===== Bắt RESPONSE để lưu log =====
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<DataBuffer> flux = Flux.from(body);
                return super.writeWith(flux.map(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    DataBufferUtils.release(dataBuffer);
                    String responseBody = new String(content, StandardCharsets.UTF_8);

                    int httpCode = getDelegate().getStatusCode() != null
                            ? getDelegate().getStatusCode().value() : 0;

                    // Lưu RESPONSE log
                    ApiLog responseLog = ApiLog.builder()
                            .requestId(UUID.randomUUID().toString())
                            .traceId(traceId)
                            .type("RESPONSE")
                            .method(method)
                            .uri(uri)
                            .resource(resource)
                            .action(action)
                            .originIp(originIp)
                            .userId(userId)
                            .httpCode(httpCode)
                            .logLevel(httpCode >= 400 ? "ERROR" : "INFO")
                            .logTime(LocalDateTime.now())
                            .timeStamp(System.currentTimeMillis() / 1000)
                            .body(responseBody)
                            .serverPort(serverPort)
                            .isNew(true)
                            .build();

                    apiLogRepository.save(responseLog)
                            .doOnError(e -> System.err.println("Lỗi lưu RESPONSE log vào PostgreSQL: " + e.getMessage()))
                            .subscribe();

                    return exchange.getResponse().bufferFactory().wrap(content);
                }));
            }
        };

        return apiLogRepository.save(requestLog)
                .doOnError(e -> System.err.println("Lỗi lưu REQUEST log vào PostgreSQL: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then(chain.filter(exchange.mutate().response(decoratedResponse).build()));
    }

    @Override
    public int getOrder() {
        return -1; // Chạy trước tất cả filter khác
    }

    private String extractResource(String uri) {
        if (uri == null) return "";
        String[] parts = uri.split("/");
        for (String part : parts) {
            if (!part.isEmpty() && !part.equals("api")) {
                return part;
            }
        }
        return uri;
    }

    private String resolveAction(String method) {
        return switch (method.toUpperCase()) {
            case "GET"         -> "read";
            case "POST"        -> "create";
            case "PUT", "PATCH"-> "update";
            case "DELETE"      -> "delete";
            default            -> "unknown";
        };
    }
}
