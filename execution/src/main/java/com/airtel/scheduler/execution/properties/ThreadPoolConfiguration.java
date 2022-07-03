package com.airtel.scheduler.execution.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@Data
public class ThreadPoolConfiguration {

    @Value(value = "${execution.coreThreadCount}")
    private Integer executionCoreThreadCount;

    @Value(value = "${execution.maxThreadCount}")
    private Integer executionMaxThreadCount;

    @Value(value = "${execution.keepAliveTimeInSec}")
    private Integer executionKeepAliveTimeInSec;

    @Value(value = "${execution.queueCapacity}")
    private Integer executionQueueCapacity;

    @Value(value = "${creation.coreThreadCount}")
    private Integer creationCoreThreadCount;

    @Value(value = "${creation.maxThreadCount}")
    private Integer creationMaxThreadCount;

    @Value(value = "${creation.keepAliveTimeInSec}")
    private Integer creationKeepAliveTimeInSec;

    @Value(value = "${creation.queueCapacity}")
    private Integer creationQueueCapacity;
}