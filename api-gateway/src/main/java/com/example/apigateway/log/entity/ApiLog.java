package com.example.apigateway.log.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("api_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiLog implements Persistable<String> {

    @Id
    @Column("request_id")
    private String requestId;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public String getId() {
        return requestId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Column("trace_id")
    private String traceId;

    @Column("type")
    private String type;

    @Column("method")
    private String method;

    @Column("uri")
    private String uri;

    @Column("resource")
    private String resource;

    @Column("action")
    private String action;

    @Column("origin_ip")
    private String originIp;

    @Column("user_id")
    private String userId;

    @Column("http_code")
    private Integer httpCode;

    @Column("log_level")
    private String logLevel;

    @Column("log_time")
    private LocalDateTime logTime;

    @Column("time_stamp")
    private Long timeStamp;

    @Column("params")
    private String params;

    @Column("header")
    private String header;

    @Column("body")
    private String body;

    @Column("server_ip")
    private String serverIp;

    @Column("server_port")
    private String serverPort;
}
