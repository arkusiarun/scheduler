package com.airtel.scheduler.execution.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
@Data
public class HttpConfiguration {

    @Value("${http.rest.maxConnections}")
    private Integer maxConnections;
    @Value("${http.rest.maxConnectionsPerHost}")
    private Integer maxConnectionsPerHost;
    @Value("${http.rest.connectionTimeOut}")
    private Integer connectionTimeout;
    @Value("${http.rest.socketTimeOut}")
    private Integer socketTimeout;
    @Value("${http.rest.connectionRequestTimeout}")
    private Integer connectionRequestTimeout;
}