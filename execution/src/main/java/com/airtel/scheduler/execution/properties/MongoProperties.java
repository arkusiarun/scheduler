package com.airtel.scheduler.execution.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arun Singh
 */

@RefreshScope
@Configuration
@Data
public class MongoProperties {

    @Value("${scheduler.mongodb.max.read.timeout}")
    private int readTimeOut;
    @Value("${scheduler.mongodb.database}")
    private String primaryDatabase;
    @Value("${scheduler.mongodb.connect.core.connection}")
    private int coreConnection;
    @Value("${scheduler.mongodb.connect.min.poolSize}")
    private int minPoolSize;
    @Value("${scheduler.mongodb.max.wait.time}")
    private int maxWaitTime;
    @Value("${scheduler.mongodb.max.connect.timeout}")
    private int connectTimeout;
    @Value("${scheduler.mongodb.max.connect.idle.timeout}")
    private int connectIdleTimeout;
    @Value("${scheduler.mongodb.max.connect.life.timeout}")
    private int connectLifeTimeout;
    @Value("${scheduler.mongodb.uri}")
    private String uri;
    @Value("${scheduler.mongodb.socketKeepAlive}")
    private boolean socketKeepAlive;
    @Value("${scheduler.mongodb.auth:}")
    private String auth;
    @Value("${scheduler.mongodb.preference: true}")
    private boolean primaryPreference;
}